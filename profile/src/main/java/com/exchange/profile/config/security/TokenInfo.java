package com.exchange.profile.config.security;

import java.util.List;


public record TokenInfo(List<String> roles, String userId, String keycloakUserId, String tokenId) {

}
