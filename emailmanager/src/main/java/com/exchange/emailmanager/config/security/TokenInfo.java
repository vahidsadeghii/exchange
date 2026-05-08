package com.exchange.emailmanager.config.security;


import java.util.List;

public record TokenInfo(List<String> roles, String userId, String tokenId) {
}