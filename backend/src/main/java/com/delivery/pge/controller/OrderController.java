package com.delivery.pge.controller;

import com.delivery.pge.dto.*;
import com.delivery.pge.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Pedidos", description = "Endpoints de criação e consulta de pedidos")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/calculate-estimate")
    @Operation(summary = "Calcular estimativa de entrega (distância, tempo e valor)")
    public ResponseEntity<EstimateResponseDTO> calculateEstimate(
            @Valid @RequestBody EstimateRequestDTO request) {
        return ResponseEntity.ok(orderService.calculateEstimate(request));
    }

    @PostMapping
    @Operation(summary = "Criar pedido confirmado (calcula e salva automaticamente)")
    public ResponseEntity<OrderResponseDTO> createOrder(
            @Valid @RequestBody OrderRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(request));
    }

    @GetMapping("/my-orders")
    @Operation(summary = "Listar pedidos do usuário autenticado (paginado)")
    public ResponseEntity<Page<OrderResponseDTO>> getMyOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(orderService.getMyOrders(page, size));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Listar pedidos por ID de usuário")
    public ResponseEntity<List<OrderResponseDTO>> getOrdersByUser(
            @PathVariable UUID userId) {
        return ResponseEntity.ok(orderService.getOrdersByUserId(userId));
    }
}
