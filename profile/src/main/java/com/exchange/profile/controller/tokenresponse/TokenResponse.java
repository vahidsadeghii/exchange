package com.exchange.profile.controller.tokenresponse;

import java.time.LocalDateTime;

public record TokenResponse (AccessTokenResponse token, LocalDateTime expirationDate){
}
