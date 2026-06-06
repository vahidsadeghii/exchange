package com.exchange.wallet.config.security;

import java.util.List;


public record TokenInfo(List<String> roles, String keycloakUserId, String tokenId) {

}
