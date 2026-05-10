package com.delivery.pge.exception;

public class MaxDistanceExceededException extends BusinessException {
    public MaxDistanceExceededException(double maxKm, double actualKm) {
        super(String.format(
            "Distância de entrega (%.1f km) excede o limite máximo permitido de %.1f km",
            actualKm, maxKm
        ));
    }
}
