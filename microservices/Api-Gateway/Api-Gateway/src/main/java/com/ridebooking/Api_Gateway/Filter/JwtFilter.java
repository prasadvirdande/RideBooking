package com.ridebooking.Api_Gateway.Filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtFilter implements GlobalFilter, Ordered {

    private final JwtService jwtService;

    public JwtFilter(JwtService jwtService) {
        this.jwtService = jwtService;
        System.out.println("JwtFilter Bean Created");
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange,
                             GatewayFilterChain chain) {

        String path = exchange.getRequest().getURI().getPath();

        System.out.println("========== JWT FILTER ==========");
        System.out.println("Path : " + path);

        // Public APIs
        if (path.startsWith("/api/auth/login") ||
                path.startsWith("/api/auth/register") ||
                path.startsWith("/api/auth/refresh-token") ||
                path.startsWith("/api/auth/driver/login") ||
                path.startsWith("/api/auth/driver/register") ||
                path.startsWith("/actuator")) {

            System.out.println("Public Endpoint");
            return chain.filter(exchange);
        }

        String authHeader =
                exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {

            System.out.println("Bearer Token Missing");

            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);

        System.out.println("Token Received");

        if (!jwtService.isTokenValid(token)) {

            System.out.println("Invalid JWT");

            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        System.out.println("JWT Valid");

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -1;
    }
}