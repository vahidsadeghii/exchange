package com.exchange.gateway.config;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SecurityGatewayFilter implements GatewayFilter {
    private final RateLimiterConfig rateLimiterConfig;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String path = exchange.getRequest().getURI().getPath();

        if (isPublic(path)) {
            return chain.filter(exchange);
        }

        String requestId = getRequestId(exchange);

        String authHeader = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange);
        }

        return rateLimiterConfig.validateRequest(requestId)
                .flatMap(valid -> {
                    if (!valid) {
                        return tooManyRequests(exchange);
                    }

                    ServerHttpRequest mutatedRequest = exchange.getRequest()
                            .mutate()
                            .header("Request-Id", requestId)
                            .build();

                    return chain.filter(
                            exchange.mutate()
                                    .request(mutatedRequest)
                                    .build()
                    );
                });
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    private Mono<Void> tooManyRequests(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        return exchange.getResponse().setComplete();
    }

    private boolean isPublic(String path) {
        return path.startsWith("/profile/public")
                || path.startsWith("/profile/open")
                || path.startsWith("/wallet/open");
    }

    private String getRequestId(ServerWebExchange exchange) {
        return Optional.ofNullable(
                exchange.getRequest().getHeaders().getFirst("Request-Id")
        ).orElse(UUID.randomUUID().toString());
    }
}