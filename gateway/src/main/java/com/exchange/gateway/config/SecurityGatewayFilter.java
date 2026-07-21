package com.exchange.gateway.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Set;


@RequiredArgsConstructor
@Slf4j
public class SecurityGatewayFilter implements GatewayFilter {
    private final GatewayConfig gatewayConfig;

    private static final Set<HttpMethod> IDEMPOTENT_METHODS =
            Set.of(HttpMethod.POST,
                    HttpMethod.PUT,
                    HttpMethod.PATCH,
                    HttpMethod.DELETE);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange,
                             GatewayFilterChain chain) {

        log.info("SECURITY FILTER URI = {}, headers={}",
                exchange.getRequest().getURI(),
                exchange.getRequest().getHeaders());

        log.info("REMOTE ADDRESS = {}",
                exchange.getRequest()
                        .getRemoteAddress());

        log.info("HEADERS HOST = {}",
                exchange.getRequest()
                        .getHeaders()
                        .getHost());

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

        HttpMethod method = exchange.getRequest().getMethod();
        boolean requiresIdempotency = IDEMPOTENT_METHODS.contains(method);

        if (!requiresIdempotency) {
            return chain.filter(exchange);
        }

        String requestId = exchange.getRequest().getHeaders().getFirst("Request-Id");

        if (requestId == null || requestId.isBlank()) {
            return badRequest(exchange, "Missing request-id");
        }
        return gatewayConfig.checkIdempotency(requestId)
                .doOnNext(valid ->
                        log.info("IDEMPOTENCY RESULT = {}", valid)
                )
                .flatMap(valid -> {
                    if (!valid) {
                        return tooManyRequests(exchange);
                    }

                    return chain.filter(exchange);
                });
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        log.error("SETTING HTTP 429");
        exchange.getResponse()
                .setStatusCode(HttpStatus.UNAUTHORIZED);

        return exchange.getResponse().setComplete();
    }

    private Mono<Void> tooManyRequests(ServerWebExchange exchange) {
        exchange.getResponse()
                .setStatusCode(HttpStatus.TOO_MANY_REQUESTS);

        return exchange.getResponse().setComplete();
    }

    private boolean isPublic(String path) {
        return path.startsWith("/profile/public")
                || path.startsWith("/profile/open")
                || path.startsWith("/wallet/open");
    }

    private Mono<Void> badRequest(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
        return exchange.getResponse().setComplete();
    }
}