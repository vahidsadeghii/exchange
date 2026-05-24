package com.exchange.gateway.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtRelayGatewayFilter implements GatewayFilter {

    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> filter(
            ServerWebExchange exchange,
            GatewayFilterChain chain
    ) {

        return exchange.getPrincipal()
                .cast(JwtAuthenticationToken.class)

                .flatMap(auth -> {
                    Jwt jwt = auth.getToken();
                    List<String> roles = extractRoles(jwt);
                    TokenInfo tokenInfo = new TokenInfo(
                            roles,
                            jwt.getSubject(),
                            jwt.getId()
                    );
                    try {
                        String json =
                                objectMapper.writeValueAsString(tokenInfo);

                        ServerHttpRequest mutated =
                                exchange.getRequest()
                                        .mutate()
                                        .header("TokenInfo", json)
                                        .build();

                        return chain.filter(
                                exchange.mutate()
                                        .request(mutated)
                                        .build()
                        );

                    } catch (Exception e) {
                        return Mono.error(e);
                    }
                })
                .switchIfEmpty(chain.filter(exchange));
    }

    private List<String> extractRoles(Jwt jwt) {
        Map<String, Object> realmAccess =
                jwt.getClaim("realm_access");
        if (realmAccess == null) {
            return List.of();
        }
        Object rolesObj = realmAccess.get("roles");

        if (!(rolesObj instanceof List<?> roles)) {
            return List.of();
        }

        return roles.stream()
                .map(String::valueOf)
                .filter(r -> r.startsWith("ROLE_"))
                .toList();
    }

}