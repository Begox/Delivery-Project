package com.delivery.pge.exception;

public class ExternalApiException extends BusinessException {
    public ExternalApiException(String message) {
        super(message);
    }
    public ExternalApiException() {
        super("Serviço de cálculo de entrega temporariamente indisponível. Tente novamente em instantes.");
    }
}
