package com.exchange.gateway.config;

import com.exchange.gateway.exception.DuplicateRequestException;
import com.exchange.gateway.exception.ErrorContent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.*;


@Configuration
@RequiredArgsConstructor
public class RouteConfig {

    @Autowired
    private RedisRateLimiter redisRateLimiter;
    private final RateLimiterConfig rateLimiterConfig;

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {

        GatewayFilter securityFilter = (exchange, chain) -> {

            String path = exchange.getRequest().getURI().getPath();

            if (path.startsWith("/public") || path.startsWith("/open")) {
                return chain.filter(exchange);
            }

            String incomingRequestId = exchange.getRequest()
                    .getHeaders()
                    .getFirst("Request-Id");

            final String requestId =
                    (incomingRequestId != null) ? incomingRequestId : UUID.randomUUID().toString();

            // Forward Request-Id
            ServerWebExchange mutated = exchange.mutate()
                    .request(req -> req.header("Request-Id", requestId))
                    .build();

            return rateLimiterConfig.validateRequest(requestId)
                    .flatMap(valid -> {

                        if (!valid) {
                            return writeError(mutated, HttpStatus.BAD_REQUEST, new DuplicateRequestException());
                        }

                        return chain.filter(mutated);
                    });
        };

        return builder.routes()
.route("profile",
                        predicateSpec ->
                                predicateSpec.path("/profile/**")
                                        .filters(
                                                filter -> filter.filter(securityFilter)
                                                        .requestRateLimiter(r -> {
                                                            r.setKeyResolver(rateLimiterConfig.keyResolver());
                                                            r.setRateLimiter(redisRateLimiter);
                                                        })
                                                        /*.requestRateLimiter(r -> {
                                                            r.setKeyResolver(rateLimiterConfig.userKeyResolver());
                                                            r.setRateLimiter(new LeakyRateLimiter(10,3000));
                                                        })*/
                                                      //  .filter(idempotencyFilter)
//                                                        .circuitBreaker(config -> {
//                                                            config.setName("gateway-instance");
//                                                            config.setFallbackUri("forward:/fallback");
//                                                        })
                                        ).uri("http://localhost:8088/profile")
//                .route("profile", r -> r
//                        .path("/profile/**")
//                        .filters(f -> f.filter(securityFilter))
//                        .uri("http://localhost:8088/profile")
//                )

)  .build();
    }

    private Mono<Void> writeError(ServerWebExchange exchange,
                                  HttpStatus status,
                                  Exception exception) {

        ErrorContent error = new ErrorContent(
                400,
                exception.getMessage(),
                LocalDateTime.now().toString(),
                exchange.getRequest().getURI().toString(),
                status.value(),
                status.name()
        );

        exchange.getResponse().setStatusCode(status);
        return writeJson(exchange, error);
    }

    private Mono<Void> writeJson(ServerWebExchange exchange, Object body) {
        try {
            byte[] bytes = new ObjectMapper().writeValueAsBytes(body);

            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

            return exchange.getResponse()
                    .writeWith(Mono.just(exchange.getResponse()
                            .bufferFactory()
                            .wrap(bytes)));

        } catch (Exception e) {
            return exchange.getResponse().setComplete();
        }
    }
}
