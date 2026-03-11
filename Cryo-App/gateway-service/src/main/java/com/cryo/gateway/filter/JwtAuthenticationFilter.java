//package com.cryo.gateway.filter;
//
//import com.cryo.common.util.JwtTokenUtil;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.cloud.gateway.filter.GatewayFilterChain;
//import org.springframework.cloud.gateway.filter.GlobalFilter;
//import org.springframework.core.Ordered;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.server.reactive.ServerHttpRequest;
//import org.springframework.http.server.reactive.ServerHttpResponse;
//import org.springframework.stereotype.Component;
//import org.springframework.util.StringUtils;
//import org.springframework.web.server.ServerWebExchange;
//import reactor.core.publisher.Mono;
//
//import java.util.List;
//
//@Component
//public class JwtAuthenticationFilter implements GlobalFilter, Ordered {
//
//    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
//
//    @Value("${jwt.secret}")
//    private String jwtSecret;
//
//    @Value("${jwt.expiration:86400}")
//    private Long jwtExpiration;
//
//    private static final List<String> PUBLIC_ENDPOINTS = List.of(
//            "/auth/signup",
//            "/auth/login",
//            "/auth/verify-otp",
//            "/auth/resend-otp"
//    );
//
//    @Override
//    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
//        ServerHttpRequest request = exchange.getRequest();
//        String path = request.getURI().getPath();
//
//        // skip public endpoints
//        if (isPublicEndpoint(path)) {
//            return chain.filter(exchange);
//        }
//
//        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
//
//        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
//            logger.warn("Missing or invalid Authorization header for path {}", path);
//            return onError(exchange, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
//        }
//
//        String token = authHeader.substring(7).trim();
//
//        if (!StringUtils.hasText(token)
//                || token.equalsIgnoreCase("null")
//                || token.equalsIgnoreCase("undefined")
//                || token.chars().filter(ch -> ch == '.').count() != 2) {
//
//            logger.warn("Invalid JWT format for path {}. Token: '{}'", path, token);
//            return onError(exchange, "Invalid token format", HttpStatus.UNAUTHORIZED);
//        }
//
//        JwtTokenUtil jwtTokenUtil = new JwtTokenUtil(jwtSecret, jwtExpiration);
//
//        // <-- Use the new access-token validator
//        if (!jwtTokenUtil.validateAccessToken(token)) {
//            logger.warn("JWT validation failed for path {}", path);
//            return onError(exchange, "Invalid or expired token", HttpStatus.UNAUTHORIZED);
//        }
//
//        try {
//            String userId = jwtTokenUtil.extractOwnerUserId(token);
//            String email = jwtTokenUtil.extractEmail(token);
//            String role = jwtTokenUtil.extractRole(token);
//
//            ServerHttpRequest modifiedRequest = request.mutate()
//                    .header("X-User-Id", userId != null ? userId : "")
//                    .header("X-User-Email", email != null ? email : "")
//                    .header("X-User-Role", role != null ? role : "")
//                    .build();
//
//            return chain.filter(exchange.mutate().request(modifiedRequest).build());
//        } catch (Exception e) {
//            logger.error("Error processing JWT token", e);
//            return onError(exchange, "Error processing token", HttpStatus.UNAUTHORIZED);
//        }
//    }
//
//    private boolean isPublicEndpoint(String path) {
//        // ✅ NEW: Allow Swagger & API Docs to pass without a token
//        if (path.contains("/v3/api-docs") ||
//                path.contains("/swagger-ui") ||
//                path.contains("/webjars")) {
//            return true;
//        }
//        return PUBLIC_ENDPOINTS.stream().anyMatch(path::startsWith);
//    }
//
//    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
//        ServerHttpResponse response = exchange.getResponse();
//        response.setStatusCode(status);
//        response.getHeaders().add("Content-Type", "application/json");
//        return response.setComplete();
//    }
//
//    @Override
//    public int getOrder() {
//        return -100;
//    }
//}
package com.cryo.gateway.filter;

import com.cryo.common.util.JwtTokenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400}")
    private Long jwtExpiration;

    private static final List<String> PUBLIC_ENDPOINTS = List.of(
            "/auth/signup",
            "/auth/login",
            "/auth/verify-otp",
            "/auth/resend-otp",

            // ✅ ADD THIS LINE: Allow acknowledgement without login
            "/alerts/acknowledge"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // skip public endpoints
        if (isPublicEndpoint(path)) {
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            return onError(exchange, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7).trim();
        JwtTokenUtil jwtTokenUtil = new JwtTokenUtil(jwtSecret, jwtExpiration);

        if (!jwtTokenUtil.validateAccessToken(token)) {
            return onError(exchange, "Invalid or expired token", HttpStatus.UNAUTHORIZED);
        }

        try {
            String userId = jwtTokenUtil.extractOwnerUserId(token);
            String email = jwtTokenUtil.extractEmail(token);
            String role = jwtTokenUtil.extractRole(token);

            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-User-Id", userId != null ? userId : "")
                    .header("X-User-Email", email != null ? email : "")
                    .header("X-User-Role", role != null ? role : "")
                    .build();

            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        } catch (Exception e) {
            return onError(exchange, "Error processing token", HttpStatus.UNAUTHORIZED);
        }
    }

    private boolean isPublicEndpoint(String path) {
        if (path.contains("/v3/api-docs") ||
                path.contains("/swagger-ui") ||
                path.contains("/webjars")) {
            return true;
        }
        return PUBLIC_ENDPOINTS.stream().anyMatch(path::startsWith);
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        return response.setComplete();
    }

    @Override
    public int getOrder() {
        return -100;
    }
}