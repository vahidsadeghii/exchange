package com.exchange.emailmanager.controller.verifyemail;

import com.exchange.emailmanager.service.EmailSenderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class VerifyEmailController {
    private final EmailSenderService emailSenderService;

    @PostMapping("/internal/verify-email")
    public void handle(@RequestBody VerifyEmailRequest request) {
        emailSenderService.mailSender(request.getEmailTo(),
                request.getVerifyCode(), request.getExpirationDate().toString());
    }

}
