package com.exchange.profile.controller.refreshtoken;

import com.exchange.profile.domain.JwtToken;
import com.exchange.profile.service.implement.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RefreshTokenController {
    private final TokenService tokenService;

    @PostMapping("/open/refresh")
    public JwtToken refresh(@RequestBody RefreshRequest request) {
        return tokenService.refreshAccessToken(request.refreshToken());
    }
}
