package com.exchange.emailmanager.config.security;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClientInfo {
    private String id;
    private String ipAddress;
    private String agentType;
}