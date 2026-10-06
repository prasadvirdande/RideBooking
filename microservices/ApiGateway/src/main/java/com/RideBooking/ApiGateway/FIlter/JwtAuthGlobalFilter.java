package com.RideBooking.ApiGateway.FIlter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthGlobalFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;

    public JwtAuthGlobalFilter(
            JwtUtil jwtUtil,
            TokenBlacklistService tokenBlacklistService) {

        this.jwtUtil = jwtUtil;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Override
    public int getOrder() {
        return -1;
    }

    @Override
    public Mono<Void> filter(
            ServerWebExchange exchange,
            GatewayFilterChain chain) {

        String uriPath = exchange.getRequest()
                .getURI()
                .getPath();


        if (uriPath.equals("/api/auth/login")
                || uriPath.equals("/api/auth/register")
                || uriPath.equals("/api/auth/driver/login")
                || uriPath.equals("/api/auth/driver/register")) {

            return chain.filter(exchange);
        }
        String authHeader = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null
                || !authHeader.startsWith("Bearer ")) {

            exchange.getResponse()
                    .setStatusCode(HttpStatus.UNAUTHORIZED);

            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);
        return tokenBlacklistService
                .isBlacklisted(token)
                .flatMap(isBlacklisted -> {


                    if (isBlacklisted) {

                        exchange.getResponse()
                                .setStatusCode(HttpStatus.UNAUTHORIZED);

                        return exchange.getResponse()
                                .setComplete();
                    }


                    try {

                        if (!jwtUtil.isTokenValid(token)) {

                            exchange.getResponse()
                                    .setStatusCode(
                                            HttpStatus.UNAUTHORIZED
                                    );

                            return exchange.getResponse()
                                    .setComplete();
                        }

                        String username =
                                jwtUtil.extractUsername(token);

                        String role =
                                jwtUtil.extractRoles(token).toString();

                        System.out.println(
                                "Username: " + username
                        );

                        System.out.println(
                                "Role: " + role
                        );


                        return chain.filter(exchange);

                    } catch (Exception e) {

                        exchange.getResponse()
                                .setStatusCode(
                                        HttpStatus.UNAUTHORIZED
                                );

                        return exchange.getResponse()
                                .setComplete();
                    }
                });
    }
}