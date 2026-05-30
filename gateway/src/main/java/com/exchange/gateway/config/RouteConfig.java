package com.exchange.gateway.config;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.GatewayFilterSpec;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RouteConfig {
    private final RedisRateLimiter redisRateLimiter;
    private final GatewayConfig gatewayConfig;
    private final SecurityGatewayFilter securityGatewayFilter;
    private final JwtRelayGatewayFilter jwtRelayGatewayFilter;
    private final KeyResolver keyResolver;

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
                                            rate.setKeyResolver(gatewayConfig.keyResolver());
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
                                            rate.setKeyResolver(gatewayConfig.keyResolver());
                                            rate.setRateLimiter(redisRateLimiter);
                                        })
                                )
                                .uri("http://localhost:8092")
                )
                .build();
    }

    private GatewayFilterSpec applyDefaultFilters(GatewayFilterSpec f) {
        f.filter(jwtRelayGatewayFilter);
        f.filter(securityGatewayFilter);
        f.requestRateLimiter(rate -> {
            rate.setKeyResolver(keyResolver);
            rate.setRateLimiter(redisRateLimiter);
        });
        return f;
    }
}


