package com.delivery.pge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class DeliveryPgeApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeliveryPgeApplication.class, args);
    }
}
