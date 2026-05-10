package com.delivery.pge.service;

import com.delivery.pge.dto.*;
import com.delivery.pge.entity.Order;
import com.delivery.pge.entity.User;
import com.delivery.pge.exception.BusinessException;
import com.delivery.pge.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserService userService;
    private final GeocodingService geocodingService;
    private final RouteService routeService;
    private final PricingService pricingService;

    /**
     * Calculates delivery estimate without creating an order.
     */
    public EstimateResponseDTO calculateEstimate(EstimateRequestDTO request) {
        log.info("Calculating estimate: {} -> {}", request.getPickupAddress(), request.getDeliveryAddress());

        GeocodingService.Coordinates origin = geocodingService.geocode(request.getPickupAddress());
        GeocodingService.Coordinates destination = geocodingService.geocode(request.getDeliveryAddress());

        RouteService.RouteInfo routeInfo = routeService.calculateRoute(origin, destination);

        BigDecimal estimatedValue = pricingService.calculateDeliveryPrice(routeInfo.distanceKm());

        return EstimateResponseDTO.builder()
                .distanceKm(BigDecimal.valueOf(routeInfo.distanceKm()).setScale(2, java.math.RoundingMode.HALF_UP))
                .estimatedTimeMinutes(routeInfo.estimatedTimeMinutes())
                .estimatedValue(estimatedValue)
                .build();
    }

    /**
     * Creates a confirmed order for the authenticated user.
     * Value is calculated by the backend — never accepted from the frontend.
     */
    @Transactional
    public OrderResponseDTO createOrder(OrderRequestDTO request) {
        User user = userService.getAuthenticatedUser();
        log.info("Creating order for user: {}", user.getId());

        // Calculate estimate (validates addresses and distance)
        EstimateRequestDTO estimateRequest = new EstimateRequestDTO();
        estimateRequest.setPickupAddress(request.getPickupAddress());
        estimateRequest.setDeliveryAddress(request.getDeliveryAddress());

        EstimateResponseDTO estimate = calculateEstimate(estimateRequest);

        if (estimate.getEstimatedValue().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Valor estimado da entrega deve ser maior que zero");
        }

        Order order = Order.builder()
                .user(user)
                .pickupAddress(request.getPickupAddress())
                .deliveryAddress(request.getDeliveryAddress())
                .itemDescription(request.getItemDescription())
                .distanceKm(estimate.getDistanceKm())
                .estimatedTimeMinutes(estimate.getEstimatedTimeMinutes())
                .estimatedValue(estimate.getEstimatedValue())
                .build();

        Order saved = orderRepository.save(order);
        log.info("Order created with ID: {}", saved.getId());
        return mapToResponse(saved);
    }

    /**
     * Lists orders for the authenticated user with pagination.
     */
    public Page<OrderResponseDTO> getMyOrders(int page, int size) {
        User user = userService.getAuthenticatedUser();
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return orderRepository.findByUserOrderByCreatedAtDesc(user, pageRequest)
                .map(this::mapToResponse);
    }

    /**
     * Lists orders for a specific user ID (admin use or same user).
     */
    public List<OrderResponseDTO> getOrdersByUserId(UUID userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private OrderResponseDTO mapToResponse(Order order) {
        return OrderResponseDTO.builder()
                .id(order.getId())
                .userId(order.getUser().getId())
                .pickupAddress(order.getPickupAddress())
                .deliveryAddress(order.getDeliveryAddress())
                .itemDescription(order.getItemDescription())
                .distanceKm(order.getDistanceKm())
                .estimatedTimeMinutes(order.getEstimatedTimeMinutes())
                .estimatedValue(order.getEstimatedValue())
                .createdAt(order.getCreatedAt())
                .status(order.getStatus())
                .build();
    }
}
