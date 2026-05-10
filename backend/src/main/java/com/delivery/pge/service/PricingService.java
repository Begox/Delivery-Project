package com.delivery.pge.service;

import com.delivery.pge.config.DeliveryConfig;
import com.delivery.pge.exception.MaxDistanceExceededException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@Service
@RequiredArgsConstructor
public class PricingService {

    private final DeliveryConfig deliveryConfig;

    /**
     * Calculates the delivery price using the formula:
     * estimatedValue = baseFee + (distanceKm * pricePerKm)
     *
     * @param distanceKm actual delivery distance in kilometers
     * @return calculated estimated value
     * @throws MaxDistanceExceededException if distance exceeds configured maximum
     */
    public BigDecimal calculateDeliveryPrice(double distanceKm) {
        log.info("Calculating delivery price for distance: {} km", distanceKm);

        if (distanceKm > deliveryConfig.getMaxDistanceKm()) {
            throw new MaxDistanceExceededException(deliveryConfig.getMaxDistanceKm(), distanceKm);
        }

        BigDecimal distance = BigDecimal.valueOf(distanceKm);
        BigDecimal priceForDistance = distance.multiply(deliveryConfig.getPricePerKm());
        BigDecimal totalPrice = deliveryConfig.getBaseFee().add(priceForDistance);

        BigDecimal result = totalPrice.setScale(2, RoundingMode.HALF_UP);
        log.info("Delivery price: R$ {}", result);

        return result;
    }
}
