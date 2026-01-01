package com.exchange.profile.config.security;


import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
@EnableReactiveMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(
                        auth ->
                                auth.requestMatchers("/publix/**")
                                        .permitAll()
                                        .requestMatchers("/open/**")
                                        .permitAll()
                                        .anyRequest()
                                        .anonymous())
                .oauth2ResourceServer(
                        oauth2 ->
                                oauth2.jwt(
                                        jwt -> jwt.jwtAuthenticationConverter(new JWTAuthenticationConverter())
                                ));
        return http.build();

    }

}
