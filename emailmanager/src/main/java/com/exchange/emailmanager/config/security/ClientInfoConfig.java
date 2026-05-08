package com.exchange.emailmanager.config.security;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.annotation.RequestScope;


@Configuration
public class ClientInfoConfig {

    @Bean
    @RequestScope
    public ClientInfo clientInfo(HttpServletRequest request){
        if(request.getHeaders("Client-Info") !=null){
            try {
                return new ObjectMapper().readValue(request.getHeader("Client-Info"), ClientInfo.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Error client info");
            }
        }else return null;
    }
}
