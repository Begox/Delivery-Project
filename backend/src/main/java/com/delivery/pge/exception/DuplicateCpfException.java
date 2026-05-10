package com.delivery.pge.exception;

public class DuplicateCpfException extends BusinessException {
    public DuplicateCpfException() {
        super("CPF já cadastrado no sistema");
    }
}
