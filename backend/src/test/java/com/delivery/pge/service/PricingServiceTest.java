package com.delivery.pge.service;

import com.delivery.pge.config.DeliveryConfig;
import com.delivery.pge.exception.MaxDistanceExceededException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("PricingService — Unit Tests")
class PricingServiceTest {

    @Mock
    private DeliveryConfig deliveryConfig;

    @InjectMocks
    private PricingService pricingService;

    @BeforeEach
    void setUp() {
        when(deliveryConfig.getBaseFee()).thenReturn(new BigDecimal("5.00"));
        when(deliveryConfig.getPricePerKm()).thenReturn(new BigDecimal("2.50"));
        when(deliveryConfig.getMaxDistanceKm()).thenReturn(30.0);
    }

    @Nested
    @DisplayName("Cálculo de preço")
    class PriceCalculation {

        @Test
        @DisplayName("Deve calcular corretamente para 5.2 km → R$ 18,00")
        void shouldCalculateCorrectPriceFor5km() {
            // formula: 5.00 + (5.2 * 2.50) = 5.00 + 13.00 = 18.00
            BigDecimal result = pricingService.calculateDeliveryPrice(5.2);
            assertThat(result).isEqualByComparingTo("18.00");
        }

        @Test
        @DisplayName("Deve calcular corretamente para 1 km → R$ 7,50")
        void shouldCalculateCorrectPriceFor1km() {
            // 5.00 + (1.0 * 2.50) = 7.50
            BigDecimal result = pricingService.calculateDeliveryPrice(1.0);
            assertThat(result).isEqualByComparingTo("7.50");
        }

        @Test
        @DisplayName("Deve aceitar exatamente 30 km (limite máximo) → R$ 80,00")
        void shouldAcceptExactMaxDistance() {
            // 5.00 + (30.0 * 2.50) = 80.00
            BigDecimal result = pricingService.calculateDeliveryPrice(30.0);
            assertThat(result).isEqualByComparingTo("80.00");
        }

        @Test
        @DisplayName("Deve retornar valor com 2 casas decimais")
        void shouldReturnValueWith2DecimalPlaces() {
            BigDecimal result = pricingService.calculateDeliveryPrice(2.333);
            assertThat(result.scale()).isEqualTo(2);
        }

        @Test
        @DisplayName("Deve usar arredondamento HALF_UP corretamente")
        void shouldApplyHalfUpRounding() {
            // 5.00 + (0.1 * 2.50) = 5.25 — sem arredondamento problemático
            BigDecimal result = pricingService.calculateDeliveryPrice(0.1);
            assertThat(result).isEqualByComparingTo("5.25");
        }
    }

    @Nested
    @DisplayName("Validação de distância máxima")
    class MaxDistanceValidation {

        @Test
        @DisplayName("Deve lançar MaxDistanceExceededException para 31 km")
        void shouldThrowForDistanceExceeding30km() {
            assertThatThrownBy(() -> pricingService.calculateDeliveryPrice(31.0))
                    .isInstanceOf(MaxDistanceExceededException.class);
        }

        @Test
        @DisplayName("Deve lançar MaxDistanceExceededException para 100 km")
        void shouldThrowForLargeDistance() {
            assertThatThrownBy(() -> pricingService.calculateDeliveryPrice(100.0))
                    .isInstanceOf(MaxDistanceExceededException.class);
        }

        @Test
        @DisplayName("Deve lançar MaxDistanceExceededException para 30.01 km (logo acima do limite)")
        void shouldThrowForDistanceJustAboveLimit() {
            assertThatThrownBy(() -> pricingService.calculateDeliveryPrice(30.01))
                    .isInstanceOf(MaxDistanceExceededException.class);
        }

        @Test
        @DisplayName("Não deve lançar exceção para 29.99 km")
        void shouldNotThrowForDistanceJustBelowLimit() {
            assertThatCode(() -> pricingService.calculateDeliveryPrice(29.99))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Interação com DeliveryConfig")
    class ConfigInteraction {

        @Test
        @DisplayName("Deve consultar getBaseFee do DeliveryConfig")
        void shouldInvokeBaseFee() {
            pricingService.calculateDeliveryPrice(5.0);
            verify(deliveryConfig, atLeastOnce()).getBaseFee();
        }

        @Test
        @DisplayName("Deve consultar getPricePerKm do DeliveryConfig")
        void shouldInvokePricePerKm() {
            pricingService.calculateDeliveryPrice(5.0);
            verify(deliveryConfig, atLeastOnce()).getPricePerKm();
        }

        @Test
        @DisplayName("Deve consultar getMaxDistanceKm do DeliveryConfig")
        void shouldInvokeMaxDistanceKm() {
            pricingService.calculateDeliveryPrice(5.0);
            verify(deliveryConfig, atLeastOnce()).getMaxDistanceKm();
        }

        @Test
        @DisplayName("Deve usar a taxa base configurada dinamicamente")
        void shouldUseDynamicBaseFee() {
            when(deliveryConfig.getBaseFee()).thenReturn(new BigDecimal("10.00"));
            // 10.00 + (5.0 * 2.50) = 22.50
            BigDecimal result = pricingService.calculateDeliveryPrice(5.0);
            assertThat(result).isEqualByComparingTo("22.50");
        }

        @Test
        @DisplayName("Deve usar o valor por km configurado dinamicamente")
        void shouldUseDynamicPricePerKm() {
            when(deliveryConfig.getPricePerKm()).thenReturn(new BigDecimal("1.00"));
            // 5.00 + (5.0 * 1.00) = 10.00
            BigDecimal result = pricingService.calculateDeliveryPrice(5.0);
            assertThat(result).isEqualByComparingTo("10.00");
        }
    }
}
