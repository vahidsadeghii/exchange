package com.exchange.gateway.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class GatewayConfig {
    private final ObjectMapper objectMapper;
    private final ReactiveRedisTemplate<String, String> redisTemplate;

    @Bean
    public RedisRateLimiter redisRateLimiter() {
        log.info("REDIS RATE LIMITER CREATED");
        return new RedisRateLimiter(1000, 2000, 1);
    }

    @Bean
    public KeyResolver keyResolver() {
        return exchange -> {

            String tokenInfoJson = exchange.getRequest()
                    .getHeaders()
                    .getFirst("TokenInfo");

            log.debug("TokenInfo = " + tokenInfoJson);
            System.out.println("TOKENINFO: " + tokenInfoJson);
            log.info("KEY RESOLVER CALLED");
            if (tokenInfoJson != null) {
                try {
                     TokenInfo info =
                        objectMapper.readValue(tokenInfoJson, TokenInfo.class);
                String key = "kc:" + info.keycloakUserId();
                log.info("RATE LIMIT KEY = {}", key);
                return Mono.just(key);
            } catch (Exception e) {
                return Mono.error(
                    new RuntimeException("Invalid TokenInfo", e)
                );
            }
        }
        String key = "ip:" + getClientIp(exchange);
        log.info("RATE LIMIT KEY = {}", key);

        return Mono.just(key);
        };
    }


    public Mono<Boolean> checkIdempotency(String requestId) {
        if (StringUtils.isEmpty(requestId)) {
            return Mono.just(true);
        }
        System.out.println("VALIDATING REQUEST ID = " + requestId);
        return redisTemplate.opsForValue()
                .get(requestId)
                .flatMap(v -> Mono.just(false))
                .switchIfEmpty(
                        redisTemplate.opsForValue()
                                .set(requestId, "1", Duration.ofMillis(2))
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