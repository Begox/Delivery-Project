package com.delivery.pge.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequestDTO {

    @NotBlank(message = "Nome completo é obrigatório")
    private String fullName;

    @NotBlank(message = "CPF é obrigatório")
    @Pattern(regexp = "\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}|\\d{11}",
             message = "CPF inválido. Use o formato 000.000.000-00 ou apenas números")
    private String cpf;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    private String email;

    @NotBlank(message = "Senha é obrigatória")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{6,}$",
        message = "Senha deve ter no mínimo 6 caracteres, uma letra maiúscula, uma minúscula e um caractere especial"
    )
    private String password;

    @NotBlank(message = "Telefone é obrigatório")
    private String phone;

    private String secondaryPhone;

    @NotBlank(message = "CEP é obrigatório")
    private String cep;

    @NotBlank(message = "Endereço é obrigatório")
    private String address;

    @NotBlank(message = "Local de referência é obrigatório")
    private String referencePoint;
}
