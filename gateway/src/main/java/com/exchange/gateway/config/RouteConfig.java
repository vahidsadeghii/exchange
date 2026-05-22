package com.exchange.gateway.config;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.*;

@Configuration
@RequiredArgsConstructor
public class RouteConfig {
    private final RedisRateLimiter redisRateLimiter;
    private final RateLimiterConfig rateLimiterConfig;
    private final SecurityGatewayFilter securityGatewayFilter;
    private final JwtRelayGatewayFilter jwtRelayGatewayFilter;

    @Bean
    public RouteLocator routeLocator(
            RouteLocatorBuilder builder
    ) {

        return builder.routes()
                .route(
                        "profile",
                        r -> r.path("/profile/**")
                                .filters(f -> f
                                        .filter(jwtRelayGatewayFilter)
                                        .filter(securityGatewayFilter)
                                        .requestRateLimiter(rate -> {
                                            rate.setKeyResolver(rateLimiterConfig.keyResolver());
                                            rate.setRateLimiter(redisRateLimiter);
                                        })
                                )
                                .uri("http://localhost:8088")
                )
                .route(
                        "wallet",
                        r -> r.path("/wallet/**")
                                .filters(f -> f
                                        .filter(jwtRelayGatewayFilter)
                                        .filter(securityGatewayFilter)
                                        .requestRateLimiter(rate -> {
                                            rate.setKeyResolver(rateLimiterConfig.keyResolver());
                                            rate.setRateLimiter(redisRateLimiter);
                                        })
                                )
                                .uri("http://localhost:8092")
                )
                .build();
    }
}


