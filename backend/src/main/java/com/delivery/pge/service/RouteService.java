package com.delivery.pge.service;

import com.delivery.pge.config.WebClientConfig;
import com.delivery.pge.exception.ExternalApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class RouteService {

    private final WebClient osrmClient;
    private final WebClientConfig webClientConfig;

    public RouteService(
            @Qualifier("osrmClient") WebClient osrmClient,
            WebClientConfig webClientConfig) {
        this.osrmClient = osrmClient;
        this.webClientConfig = webClientConfig;
    }

    public record RouteInfo(double distanceKm, int estimatedTimeMinutes) {}

    /**
     * Calculates route between two coordinates using OSRM.
     *
     * @param origin      Origin coordinates
     * @param destination Destination coordinates
     * @return RouteInfo with distance and estimated time
     * @throws ExternalApiException if OSRM is unavailable or route cannot be calculated
     */
    public RouteInfo calculateRoute(
            GeocodingService.Coordinates origin,
            GeocodingService.Coordinates destination) {

        log.info("Calculating route from ({},{}) to ({},{})",
                origin.lat(), origin.lon(), destination.lat(), destination.lon());

        String coordinates = String.format("%s,%s;%s,%s",
                origin.lon(), origin.lat(),
                destination.lon(), destination.lat());

        try {
            Map<String, Object> response = osrmClient.get()
                    .uri("/route/v1/driving/" + coordinates + "?overview=false&steps=false")
                    .retrieve()
                    .bodyToMono(Map.class)
                    .cast(Map.class)
                    .map(m -> (Map<String, Object>) m)
                    .timeout(webClientConfig.getTimeout())
                    .block();

            if (response == null) {
                throw new ExternalApiException("Rota indisponível para os endereços informados");
            }

            String code = (String) response.get("code");
            if (!"Ok".equals(code)) {
                throw new ExternalApiException("Rota não encontrada entre os endereços informados");
            }

            List<Map<String, Object>> routes = (List<Map<String, Object>>) response.get("routes");
            if (routes == null || routes.isEmpty()) {
                throw new ExternalApiException("Nenhuma rota disponível para os endereços informados");
            }

            Map<String, Object> route = routes.get(0);
            double distanceMeters = ((Number) route.get("distance")).doubleValue();
            double durationSeconds = ((Number) route.get("duration")).doubleValue();

            double distanceKm = distanceMeters / 1000.0;
            int estimatedTimeMinutes = (int) Math.ceil(durationSeconds / 60.0);

            log.info("Route: distance={}km, time={}min", distanceKm, estimatedTimeMinutes);
            return new RouteInfo(distanceKm, estimatedTimeMinutes);

        } catch (ExternalApiException e) {
            throw e;
        } catch (WebClientRequestException e) {
            log.error("OSRM API unavailable", e);
            throw new ExternalApiException();
        } catch (Exception e) {
            log.error("Error calculating route", e);
            throw new ExternalApiException("Falha no cálculo de rota: " + e.getMessage());
        }
    }
}
