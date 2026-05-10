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
public class GeocodingService {

    private final WebClient nominatimClient;
    private final WebClientConfig webClientConfig;

    public GeocodingService(
            @Qualifier("nominatimClient") WebClient nominatimClient,
            WebClientConfig webClientConfig) {
        this.nominatimClient = nominatimClient;
        this.webClientConfig = webClientConfig;
    }

    public record Coordinates(double lat, double lon) {}

    /**
     * Converts a full address string to geographic coordinates using Nominatim.
     *
     * @param address Full address (e.g., "Av. Paulista, 1000, São Paulo - SP")
     * @return Coordinates (lat, lon)
     * @throws ExternalApiException if the API is unavailable or the address is not found
     */
    public Coordinates geocode(String address) {
        log.info("Geocoding address: {}", address);

        try {
            List<Map<String, Object>> results = nominatimClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/search")
                            .queryParam("q", address)
                            .queryParam("format", "json")
                            .queryParam("limit", 1)
                            .queryParam("countrycodes", "br")
                            .build())
                    .retrieve()
                    .bodyToFlux(Map.class)
                    .cast(Map.class)
                    .map(m -> (Map<String, Object>) m)
                    .collectList()
                    .timeout(webClientConfig.getTimeout())
                    .block();

            if (results == null || results.isEmpty()) {
                throw new ExternalApiException("Endereço não encontrado: " + address);
            }

            Map<String, Object> firstResult = results.get(0);
            double lat = Double.parseDouble((String) firstResult.get("lat"));
            double lon = Double.parseDouble((String) firstResult.get("lon"));

            log.info("Geocoded '{}' => lat={}, lon={}", address, lat, lon);
            return new Coordinates(lat, lon);

        } catch (ExternalApiException e) {
            throw e;
        } catch (WebClientRequestException e) {
            log.error("Nominatim API unavailable", e);
            throw new ExternalApiException();
        } catch (Exception e) {
            log.error("Error geocoding address: {}", address, e);
            throw new ExternalApiException("Falha ao geocodificar endereço: " + e.getMessage());
        }
    }
}
