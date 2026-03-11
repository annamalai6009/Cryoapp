package com.cryo.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth-service", r -> r
                        .path("/auth/**")
                        .uri("lb://auth-service"))
                .route("freezer-service", r -> r
                        .path("/freezers/**")
                        .uri("lb://freezer-service"))
                .route("export-service", r -> r
                        .path("/export/**")
                        .uri("lb://export-service"))
                // <-- Add this
                .route("alert-service", r -> r
                        .path("/alerts/**")
                        .uri("lb://alert-service"))
                .build();
    }

}

