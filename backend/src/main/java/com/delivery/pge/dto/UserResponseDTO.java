package com.delivery.pge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {

    private UUID id;
    private String fullName;
    private String cpf;
    private String email;
    private String phone;
    private String secondaryPhone;
    private String cep;
    private String address;
    private String referencePoint;
    private LocalDateTime createdAt;
}
