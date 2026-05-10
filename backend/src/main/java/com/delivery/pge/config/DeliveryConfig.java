package com.delivery.pge.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@ConfigurationProperties(prefix = "delivery")
@Data
public class DeliveryConfig {

    /**
     * Base fee applied to every delivery regardless of distance (in BRL).
     */
    private BigDecimal baseFee = new BigDecimal("5.00");

    /**
     * Price per kilometer (in BRL).
     */
    private BigDecimal pricePerKm = new BigDecimal("2.50");

    /**
     * Maximum allowed delivery distance in kilometers.
     */
    private double maxDistanceKm = 30.0;
}
