package com.exchange.gateway.config;

import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange,
                             GatewayFilterChain chain) {

        String path = exchange.getRequest().getURI().getPath();

        if (isPublic(path)) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange);
        }

        String requestId = buildRequestId(exchange);

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

    private String buildRequestId(ServerWebExchange exchange) {
        try {
            String tokenInfoHeader = exchange.getRequest()
                    .getHeaders()
                    .getFirst("TokenInfo");
            if (tokenInfoHeader != null) {
                TokenInfo tokenInfo = objectMapper.readValue(
                        tokenInfoHeader,
                        TokenInfo.class
                );

                if (tokenInfo.tokenId() != null) {
                    return tokenInfo.tokenId() + ":" + UUID.randomUUID();
                }
            }
        } catch (Exception ignored) {

        }

        return UUID.randomUUID().toString();
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
}