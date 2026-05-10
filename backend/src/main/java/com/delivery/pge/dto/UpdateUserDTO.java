package com.delivery.pge.dto;

import lombok.Data;

@Data
public class UpdateUserDTO {

    private String phone;
    private String secondaryPhone;
    private String cep;
    private String address;
    private String referencePoint;
}
