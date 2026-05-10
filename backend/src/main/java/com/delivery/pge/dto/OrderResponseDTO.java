package com.delivery.pge.dto;

import com.delivery.pge.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponseDTO {

    private UUID id;
    private UUID userId;
    private String pickupAddress;
    private String deliveryAddress;
    private String itemDescription;
    private BigDecimal distanceKm;
    private Integer estimatedTimeMinutes;
    private BigDecimal estimatedValue;
    private LocalDateTime createdAt;
    private OrderStatus status;
}
