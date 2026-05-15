package com.exchange.profile.controller.tokenresponse;

import java.time.LocalDateTime;

public record RefreshTokenResponse (String refreshToken, LocalDateTime expirationDate){
}
