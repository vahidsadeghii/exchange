package com.exchange.gateway.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Optional;


@Configuration
@RequiredArgsConstructor
public class RateLimiterConfig {
    private final ObjectMapper objectMapper;
    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

@Bean
public KeyResolver keyResolver() {
    return exchange -> {

        String tokenInfoJson = exchange.getRequest()
                .getHeaders()
                .getFirst("TokenInfo");

        if (tokenInfoJson != null) {
            try {
                TokenInfo info = objectMapper.readValue(tokenInfoJson, TokenInfo.class);
                return Mono.just("kc:" + info.keycloakUserId());

            } catch (Exception e) {
                return Mono.error(new RuntimeException("Invalid TokenInfo", e));
            }
        }

        // fallback only for truly anonymous requests
        return Mono.just("ip:" + getClientIp(exchange));
    };
}

    @Bean
    public RedisRateLimiter redisRateLimiter() {
        return new RedisRateLimiter(1000, 2000, 1);
    }

    public Mono<Boolean> validateRequest(String requestId) {
        if (requestId == null) {
            return Mono.just(true);
        }

        return reactiveRedisTemplate.opsForValue()
                .get(requestId)
                .flatMap(v -> Mono.just(false))
                .switchIfEmpty(
                        reactiveRedisTemplate.opsForValue()
                                .set(requestId, "1", Duration.ofSeconds(2))
                                .thenReturn(true)
                );
    }

    private String getClientIp(ServerWebExchange exchange) {
        return Optional.ofNullable(exchange.getRequest().getRemoteAddress())
                .map(addr -> addr.getAddress().getHostAddress())
                .orElse("anonymous");
    }
}