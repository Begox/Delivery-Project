package com.delivery.pge.exception;

public class DuplicateEmailException extends BusinessException {
    public DuplicateEmailException() {
        super("Email já cadastrado no sistema");
    }
}
