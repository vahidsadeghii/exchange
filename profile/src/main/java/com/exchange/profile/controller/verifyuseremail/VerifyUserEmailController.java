package com.exchange.profile.controller.verifyuseremail;



import com.exchange.profile.controller.tokenresponse.AccessTokenResponse;
import com.exchange.profile.controller.tokenresponse.RefreshTokenResponse;

import com.exchange.profile.domain.TokenResponse;
import com.exchange.profile.service.VerifySentEmailHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class VerifyUserEmailController {
    private final VerifySentEmailHistoryService verifySentEmailHistoryService;

     @PostMapping(value = "/open/verify-code")
    public VerifyUserEmailResponse handle(@RequestBody VerifyUserEmailRequest request) {
        TokenResponse token = verifySentEmailHistoryService.verifyEmailCode(request.verifySentEmailHistoryId(), request.verifyCode(), request.expiredDate());
        return new VerifyUserEmailResponse(new AccessTokenResponse(token.accessToken().accessToken(), token.accessToken().expirationDate())
                , new RefreshTokenResponse(token.refreshToken().refreshToken(), token.refreshToken().expirationDate()));
    }
}
