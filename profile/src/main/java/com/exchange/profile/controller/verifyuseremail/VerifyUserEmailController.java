package com.exchange.profile.controller.verifyuseremail;


import com.exchange.profile.domain.VerifySentEmailHistory;
import com.exchange.profile.service.VerifySentEmailHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class VerifyUserEmailController {

    private final VerifySentEmailHistoryService verifySentEmailHistoryService;


    @PostMapping(value = "/open/verify-email")
    public VerifyUserEmailResponse handle(@RequestBody VerifyUserEmailRequest request) {

        VerifySentEmailHistory response = verifySentEmailHistoryService.registerEmail(request.email());

        return new VerifyUserEmailResponse(response.getId(), response.getVerificationCode(), response.getExpiredDate());
    }
}
