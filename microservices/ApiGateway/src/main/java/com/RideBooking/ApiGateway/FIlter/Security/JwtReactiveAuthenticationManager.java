package com.RideBooking.ApiGateway.FIlter.Security;
import com.RideBooking.ApiGateway.FIlter.JwtUtil;
import com.RideBooking.ApiGateway.FIlter.TokenBlacklistService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtReactiveAuthenticationManager
        implements ReactiveAuthenticationManager {

    private final JwtUtil jwtUtil;

    private final TokenBlacklistService tokenBlacklistService;

    @Override
    public Mono<Authentication> authenticate(
            Authentication authentication) {

        String token = authentication.getCredentials().toString();

        return tokenBlacklistService.isBlacklisted(token)

                .flatMap(isBlacklisted -> {

                    if (isBlacklisted) {

                        return Mono.error(
                                new BadCredentialsException(
                                        "Token has been revoked"
                                )
                        );
                    }

                    try {

                        if (!jwtUtil.isTokenValid(token)) {

                            return Mono.error(
                                    new BadCredentialsException(
                                            "Invalid or expired token"
                                    )
                            );
                        }

                        String username =
                                jwtUtil.extractUsername(token);

                        List<SimpleGrantedAuthority> authorities =
                                jwtUtil.extractRoles(token)
                                        .stream()
                                        .map(role -> role.startsWith("ROLE_")
                                                ? role
                                                : "ROLE_" + role)
                                        .map(SimpleGrantedAuthority::new)
                                        .toList();

                        Authentication authenticated =
                                new UsernamePasswordAuthenticationToken(
                                        username,
                                        token,
                                        authorities
                                );

                        return Mono.just(authenticated);

                    } catch (Exception exception) {

                        return Mono.error(
                                new BadCredentialsException(
                                        "JWT authentication failed",
                                        exception
                                )
                        );
                    }
                });
    }
}
