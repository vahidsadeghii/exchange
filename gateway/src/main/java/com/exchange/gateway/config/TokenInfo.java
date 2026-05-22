package com.exchange.gateway.config;

import java.util.List;


public record TokenInfo(List<String> roles,  String keycloakUserId,  String tokenId) {

}
