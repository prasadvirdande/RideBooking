package com.RideBooking.ApiGateway.FIlter;



import com.RideBooking.ApiGateway.FIlter.Security.JwtAuthenticationConverter;
import com.RideBooking.ApiGateway.FIlter.Security.JwtReactiveAuthenticationManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpStatus;

import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;

import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.ServerAuthenticationEntryPointFailureHandler;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;

import org.springframework.security.web.server.authorization.HttpStatusServerAccessDeniedHandler;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private final JwtReactiveAuthenticationManager authenticationManager;
    private final JwtAuthenticationConverter authenticationConverter;

    public SecurityConfig(
            JwtReactiveAuthenticationManager authenticationManager,
            JwtAuthenticationConverter authenticationConverter) {

        this.authenticationManager = authenticationManager;
        this.authenticationConverter = authenticationConverter;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity http) {

        AuthenticationWebFilter authenticationWebFilter =
                new AuthenticationWebFilter(authenticationManager);

        authenticationWebFilter.setServerAuthenticationConverter(
                authenticationConverter
        );

        authenticationWebFilter.setSecurityContextRepository(
                NoOpServerSecurityContextRepository.getInstance()
        );

        authenticationWebFilter.setAuthenticationFailureHandler(
                new ServerAuthenticationEntryPointFailureHandler(
                        new HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED)
                )
        );

        return http

                .csrf(ServerHttpSecurity.CsrfSpec::disable)

                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)

                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)

                .securityContextRepository(
                        NoOpServerSecurityContextRepository.getInstance()
                )

                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(
                                new HttpStatusServerEntryPoint(
                                        HttpStatus.UNAUTHORIZED
                                )
                        )
                        .accessDeniedHandler(
                                new HttpStatusServerAccessDeniedHandler(
                                        HttpStatus.FORBIDDEN
                                )
                        )
                )

                .authorizeExchange(exchange -> exchange

                        .pathMatchers(
                                "/api/auth/login",
                                "/api/auth/register",
                                "/api/auth/driver/login",
                                "/api/auth/driver/register"
                        ).permitAll()
                        .pathMatchers("/api/user/**")
                        .hasAuthority("ROLE_RIDER")
                        .pathMatchers("/api/driver/**")
                       .hasAuthority("ROLE_DRIVER")
                        .anyExchange().authenticated()
                )

                .addFilterAt(
                        authenticationWebFilter,
                        SecurityWebFiltersOrder.AUTHENTICATION
                )

                .build();
    }
}