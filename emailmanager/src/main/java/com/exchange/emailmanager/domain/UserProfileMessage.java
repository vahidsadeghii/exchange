package com.exchange.emailmanager.domain;

public record UserProfileMessage(
        String firstName,
        String lastName,
        String email
) {

}
