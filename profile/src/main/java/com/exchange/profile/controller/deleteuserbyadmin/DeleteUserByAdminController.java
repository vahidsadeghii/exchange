package com.exchange.profile.controller.deleteuserbyadmin;


import com.exchange.profile.service.VerifySentEmailHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
public class DeleteUserByAdminController {
    private final VerifySentEmailHistoryService verifySentEmailHistoryService;


    @DeleteMapping(value = "${api.prefix.open}/user")
    public void handle(@RequestParam String userEmail) {
        verifySentEmailHistoryService.deleteUserByEmail(userEmail);
    }
}
