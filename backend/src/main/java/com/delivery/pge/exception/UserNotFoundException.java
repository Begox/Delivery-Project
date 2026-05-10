package com.delivery.pge.exception;

public class UserNotFoundException extends BusinessException {
    public UserNotFoundException() {
        super("Usuário não encontrado");
    }
    public UserNotFoundException(String message) {
        super(message);
    }
}
