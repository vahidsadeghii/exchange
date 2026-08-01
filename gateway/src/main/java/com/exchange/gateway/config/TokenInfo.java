package com.exchange.gateway.config;

import java.util.List;


public record TokenInfo(List<String> roles, Long internalUserId,  String keycloakUserId,  String tokenId) {

}
