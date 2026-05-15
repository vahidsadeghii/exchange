package com.exchange.profile.controller.tokenresponse;

import java.time.LocalDateTime;

public record AccessTokenResponse (String accessToken, LocalDateTime expirationDate) {
}
