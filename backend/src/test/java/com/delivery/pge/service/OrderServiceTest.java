package com.delivery.pge.service;

import com.delivery.pge.dto.*;
import com.delivery.pge.entity.Order;
import com.delivery.pge.entity.OrderStatus;
import com.delivery.pge.entity.User;
import com.delivery.pge.exception.BusinessException;
import com.delivery.pge.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("OrderService — Unit Tests")
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private UserService userService;
    @Mock private GeocodingService geocodingService;
    @Mock private RouteService routeService;
    @Mock private PricingService pricingService;

    @InjectMocks
    private OrderService orderService;

    // ─── Fixtures ───────────────────────────────────────────────────

    private User mockUser() {
        return User.builder()
                .id(UUID.randomUUID())
                .fullName("Maria Santos")
                .email("maria@email.com")
                .cpf("98765432100")
                .build();
    }

    private GeocodingService.Coordinates originCoords() {
        return new GeocodingService.Coordinates(-23.5613, -46.6564);
    }

    private GeocodingService.Coordinates destinationCoords() {
        return new GeocodingService.Coordinates(-23.5523, -46.6451);
    }

    private Order savedOrder(User user) {
        return Order.builder()
                .id(UUID.randomUUID())
                .user(user)
                .pickupAddress("Av. Paulista, 1000, São Paulo - SP")
                .deliveryAddress("Rua Augusta, 500, São Paulo - SP")
                .itemDescription("2x Pizza Margherita")
                .distanceKm(new BigDecimal("5.20"))
                .estimatedTimeMinutes(20)
                .estimatedValue(new BigDecimal("18.00"))
                .createdAt(LocalDateTime.now())
                .status(OrderStatus.PENDING)
                .build();
    }

    // ─── calculateEstimate() ────────────────────────────────────────

    @Nested
    @DisplayName("calculateEstimate()")
    class CalculateEstimate {

        @BeforeEach
        void setUp() {
            when(geocodingService.geocode("Av. Paulista, 1000, São Paulo - SP"))
                    .thenReturn(originCoords());
            when(geocodingService.geocode("Rua Augusta, 500, São Paulo - SP"))
                    .thenReturn(destinationCoords());
            when(routeService.calculateRoute(originCoords(), destinationCoords()))
                    .thenReturn(new RouteService.RouteInfo(5.2, 20));
            when(pricingService.calculateDeliveryPrice(5.2))
                    .thenReturn(new BigDecimal("18.00"));
        }

        @Test
        @DisplayName("Deve retornar distância correta")
        void shouldReturnCorrectDistance() {
            EstimateRequestDTO req = new EstimateRequestDTO();
            req.setPickupAddress("Av. Paulista, 1000, São Paulo - SP");
            req.setDeliveryAddress("Rua Augusta, 500, São Paulo - SP");

            EstimateResponseDTO result = orderService.calculateEstimate(req);

            assertThat(result.getDistanceKm()).isEqualByComparingTo("5.20");
        }

        @Test
        @DisplayName("Deve retornar tempo estimado correto")
        void shouldReturnCorrectEstimatedTime() {
            EstimateRequestDTO req = new EstimateRequestDTO();
            req.setPickupAddress("Av. Paulista, 1000, São Paulo - SP");
            req.setDeliveryAddress("Rua Augusta, 500, São Paulo - SP");

            EstimateResponseDTO result = orderService.calculateEstimate(req);

            assertThat(result.getEstimatedTimeMinutes()).isEqualTo(20);
        }

        @Test
        @DisplayName("Deve retornar valor calculado pelo PricingService")
        void shouldReturnValueFromPricingService() {
            EstimateRequestDTO req = new EstimateRequestDTO();
            req.setPickupAddress("Av. Paulista, 1000, São Paulo - SP");
            req.setDeliveryAddress("Rua Augusta, 500, São Paulo - SP");

            EstimateResponseDTO result = orderService.calculateEstimate(req);

            assertThat(result.getEstimatedValue()).isEqualByComparingTo("18.00");
        }

        @Test
        @DisplayName("Deve chamar GeocodingService para ambos os endereços")
        void shouldGeocodeBothAddresses() {
            EstimateRequestDTO req = new EstimateRequestDTO();
            req.setPickupAddress("Av. Paulista, 1000, São Paulo - SP");
            req.setDeliveryAddress("Rua Augusta, 500, São Paulo - SP");

            orderService.calculateEstimate(req);

            verify(geocodingService).geocode("Av. Paulista, 1000, São Paulo - SP");
            verify(geocodingService).geocode("Rua Augusta, 500, São Paulo - SP");
        }

        @Test
        @DisplayName("Deve chamar PricingService com a distância do RouteService")
        void shouldCallPricingWithDistanceFromRoute() {
            EstimateRequestDTO req = new EstimateRequestDTO();
            req.setPickupAddress("Av. Paulista, 1000, São Paulo - SP");
            req.setDeliveryAddress("Rua Augusta, 500, São Paulo - SP");

            orderService.calculateEstimate(req);

            verify(pricingService).calculateDeliveryPrice(5.2);
        }
    }

    // ─── createOrder() ──────────────────────────────────────────────

    @Nested
    @DisplayName("createOrder()")
    class CreateOrder {

        private User user;

        @BeforeEach
        void setUp() {
            user = mockUser();
            when(userService.getAuthenticatedUser()).thenReturn(user);
            when(geocodingService.geocode(anyString()))
                    .thenReturn(originCoords())
                    .thenReturn(destinationCoords());
            when(routeService.calculateRoute(any(), any()))
                    .thenReturn(new RouteService.RouteInfo(5.2, 20));
            when(pricingService.calculateDeliveryPrice(5.2))
                    .thenReturn(new BigDecimal("18.00"));
            when(orderRepository.save(any(Order.class))).thenReturn(savedOrder(user));
        }

        @Test
        @DisplayName("Deve criar pedido com status PENDING")
        void shouldCreateOrderWithPendingStatus() {
            OrderRequestDTO req = new OrderRequestDTO();
            req.setPickupAddress("Av. Paulista, 1000, São Paulo - SP");
            req.setDeliveryAddress("Rua Augusta, 500, São Paulo - SP");
            req.setItemDescription("2x Pizza");

            OrderResponseDTO result = orderService.createOrder(req);

            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo(OrderStatus.PENDING);
        }

        @Test
        @DisplayName("Deve associar o pedido ao usuário autenticado")
        void shouldAssignOrderToAuthenticatedUser() {
            OrderRequestDTO req = new OrderRequestDTO();
            req.setPickupAddress("Av. Paulista, 1000, São Paulo - SP");
            req.setDeliveryAddress("Rua Augusta, 500, São Paulo - SP");
            req.setItemDescription("Item");

            OrderResponseDTO result = orderService.createOrder(req);

            assertThat(result.getUserId()).isEqualTo(user.getId());
        }

        @Test
        @DisplayName("Deve salvar o pedido no repositório exatamente uma vez")
        void shouldSaveOrderOnce() {
            OrderRequestDTO req = new OrderRequestDTO();
            req.setPickupAddress("Av. Paulista, 1000, São Paulo - SP");
            req.setDeliveryAddress("Rua Augusta, 500, São Paulo - SP");
            req.setItemDescription("Item");

            orderService.createOrder(req);

            verify(orderRepository, times(1)).save(any(Order.class));
        }

        @Test
        @DisplayName("Deve lançar BusinessException quando valor estimado for zero")
        void shouldThrowWhenEstimatedValueIsZero() {
            when(pricingService.calculateDeliveryPrice(anyDouble()))
                    .thenReturn(BigDecimal.ZERO);

            OrderRequestDTO req = new OrderRequestDTO();
            req.setPickupAddress("Endereço A");
            req.setDeliveryAddress("Endereço B");
            req.setItemDescription("Item");

            assertThatThrownBy(() -> orderService.createOrder(req))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("maior que zero");

            verify(orderRepository, never()).save(any(Order.class));
        }

        @Test
        @DisplayName("Deve obter usuário autenticado antes de criar o pedido")
        void shouldGetAuthenticatedUser() {
            OrderRequestDTO req = new OrderRequestDTO();
            req.setPickupAddress("Av. Paulista, 1000, São Paulo - SP");
            req.setDeliveryAddress("Rua Augusta, 500, São Paulo - SP");
            req.setItemDescription("Item");

            orderService.createOrder(req);

            verify(userService, times(1)).getAuthenticatedUser();
        }
    }

    // ─── getMyOrders() ──────────────────────────────────────────────

    @Nested
    @DisplayName("getMyOrders()")
    class GetMyOrders {

        @Test
        @DisplayName("Deve retornar página de pedidos para o usuário autenticado")
        void shouldReturnPageOfOrders() {
            User user = mockUser();
            when(userService.getAuthenticatedUser()).thenReturn(user);
            when(orderRepository.findByUserOrderByCreatedAtDesc(eq(user), any(PageRequest.class)))
                    .thenReturn(new PageImpl<>(List.of(savedOrder(user))));

            Page<OrderResponseDTO> result = orderService.getMyOrders(0, 10);

            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Deve retornar página vazia quando não há pedidos")
        void shouldReturnEmptyPage() {
            User user = mockUser();
            when(userService.getAuthenticatedUser()).thenReturn(user);
            when(orderRepository.findByUserOrderByCreatedAtDesc(eq(user), any(PageRequest.class)))
                    .thenReturn(Page.empty());

            Page<OrderResponseDTO> result = orderService.getMyOrders(0, 10);

            assertThat(result.getContent()).isEmpty();
        }

        @Test
        @DisplayName("Deve respeitar os parâmetros de paginação")
        void shouldRespectPageParameters() {
            User user = mockUser();
            when(userService.getAuthenticatedUser()).thenReturn(user);
            when(orderRepository.findByUserOrderByCreatedAtDesc(eq(user), any(PageRequest.class)))
                    .thenReturn(Page.empty());

            orderService.getMyOrders(2, 5);

            ArgumentCaptor<PageRequest> captor = ArgumentCaptor.forClass(PageRequest.class);
            verify(orderRepository).findByUserOrderByCreatedAtDesc(eq(user), captor.capture());
            assertThat(captor.getValue().getPageNumber()).isEqualTo(2);
            assertThat(captor.getValue().getPageSize()).isEqualTo(5);
        }
    }

    // ─── getOrdersByUserId() ────────────────────────────────────────

    @Nested
    @DisplayName("getOrdersByUserId()")
    class GetOrdersByUserId {

        @Test
        @DisplayName("Deve retornar pedidos para userId válido")
        void shouldReturnOrdersForValidUserId() {
            User user = mockUser();
            UUID userId = user.getId();
            when(orderRepository.findByUserIdOrderByCreatedAtDesc(userId))
                    .thenReturn(List.of(savedOrder(user)));

            List<OrderResponseDTO> result = orderService.getOrdersByUserId(userId);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getUserId()).isEqualTo(userId);
        }

        @Test
        @DisplayName("Deve retornar lista vazia para usuário sem pedidos")
        void shouldReturnEmptyListForUserWithNoOrders() {
            UUID userId = UUID.randomUUID();
            when(orderRepository.findByUserIdOrderByCreatedAtDesc(userId)).thenReturn(List.of());

            List<OrderResponseDTO> result = orderService.getOrdersByUserId(userId);

            assertThat(result).isEmpty();
        }
    }
}
