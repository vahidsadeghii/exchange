package com.exchange.profile.controller.registeruseremail;


import com.exchange.profile.domain.VerifySentEmailHistory;
import com.exchange.profile.service.VerifySentEmailHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RegisterUserEmailController {

    private final VerifySentEmailHistoryService verifySentEmailHistoryService;


    @PostMapping(value = "${api.prefix.open}/verify-email")
    public RegisterUserEmailResponse handle(@RequestBody RegisterUserEmailRequest request) {
        VerifySentEmailHistory response = verifySentEmailHistoryService.registerEmail(request.email());

        return new RegisterUserEmailResponse(response.getId().toString(), response.getVerificationCode(), response.getExpiredDate());
    }
}
