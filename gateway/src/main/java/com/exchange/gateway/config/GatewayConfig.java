package com.exchange.gateway.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.common.util.StringUtils;
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
public class GatewayConfig {
    private final ObjectMapper objectMapper;
    private final ReactiveRedisTemplate<String, String> redisTemplate;

    @Bean
    public RedisRateLimiter redisRateLimiter() {
        return new RedisRateLimiter(1000, 2000, 1);
    }

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


    public Mono<Boolean> validateRequest(String requestId) {
        if (StringUtils.isEmpty(requestId)) {
            return Mono.just(true);
        }

        return redisTemplate.opsForValue()
                .get(requestId)
                .flatMap(v -> Mono.just(false))
                .switchIfEmpty(
                        redisTemplate.opsForValue()
                                .set(requestId, "1", Duration.ofSeconds(2))
                                .thenReturn(true)
                );
    }

    private String getClientIp(ServerWebExchange exchange) {
        String forwardedFor = exchange.getRequest()
                .getHeaders().getFirst("X-Forwarded-For");

        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return Optional.ofNullable(exchange.getRequest().getRemoteAddress())
                .map(addr -> addr.getAddress().getHostAddress())
                .orElse("anonymous");
    }
}