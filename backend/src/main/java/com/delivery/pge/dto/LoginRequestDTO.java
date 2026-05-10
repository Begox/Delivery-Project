package com.delivery.pge.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequestDTO {

    @NotBlank(message = "CPF ou email é obrigatório")
    private String identifier;

    @NotBlank(message = "Senha é obrigatória")
    private String password;
}
