package com.delivery.pge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstimateResponseDTO {

    private BigDecimal distanceKm;
    private Integer estimatedTimeMinutes;
    private BigDecimal estimatedValue;
}
