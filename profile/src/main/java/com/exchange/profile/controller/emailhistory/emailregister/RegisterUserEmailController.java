package com.exchange.profile.controller.emailhistory.emailregister;


import com.exchange.profile.service.VerifySentEmailHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RegisterUserEmailController {
    private final VerifySentEmailHistoryService verifySentEmailHistoryService;


    public void handle(@RequestParam String email) {

        verifySentEmailHistoryService.registerEmail(email);

    }
}
