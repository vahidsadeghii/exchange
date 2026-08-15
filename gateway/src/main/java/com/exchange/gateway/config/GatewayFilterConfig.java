package com.exchange.gateway.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class GatewayFilterConfig {
    private final GatewayConfig gatewayConfig;
    private final ObjectMapper objectMapper;

    @Bean
    public JwtRelayGatewayFilter jwtRelayGatewayFilter() {
        return new JwtRelayGatewayFilter(objectMapper);
    }

    @Bean
    public SecurityGatewayFilter securityGatewayFilter() {
        return new SecurityGatewayFilter(gatewayConfig);
    }
}
