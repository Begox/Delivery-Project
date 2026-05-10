package com.delivery.pge.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@Configuration
public class WebClientConfig {

    @Value("${external.nominatim.url}")
    private String nominatimUrl;

    @Value("${external.osrm.url}")
    private String osrmUrl;

    @Value("${external.api.timeout-seconds}")
    private int timeoutSeconds;

    @Bean("nominatimClient")
    public WebClient nominatimWebClient() {
        return WebClient.builder()
                .baseUrl(nominatimUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.USER_AGENT, "DeliveryPGE/1.0 (contato@deliverypge.com)")
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Bean("osrmClient")
    public WebClient osrmWebClient() {
        return WebClient.builder()
                .baseUrl(osrmUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public Duration getTimeout() {
        return Duration.ofSeconds(timeoutSeconds);
    }
}
