package com.delivery.pge.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OrderRequestDTO {

    @NotBlank(message = "Endereço de coleta é obrigatório")
    private String pickupAddress;

    @NotBlank(message = "Endereço de entrega é obrigatório")
    private String deliveryAddress;

    @NotBlank(message = "Descrição dos itens é obrigatória")
    private String itemDescription;

    // estimatedValue must NOT be received from frontend — calculated by backend
}
