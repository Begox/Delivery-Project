package com.delivery.pge.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EstimateRequestDTO {

    @NotBlank(message = "Endereço de coleta é obrigatório")
    private String pickupAddress;

    @NotBlank(message = "Endereço de entrega é obrigatório")
    private String deliveryAddress;
}
