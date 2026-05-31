package com.exchange.wallet.controller.wallet.updatewallet;


import com.exchange.wallet.config.security.OnlineUser;
import com.exchange.wallet.domain.Wallet;
import com.exchange.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class UpdateWalletController {
    private final WalletService walletService;

    @PostMapping(value = "${api.prefix.secure}/user/wallet")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Map<String, Wallet> handle(@AuthenticationPrincipal OnlineUser onlineUser,
                                      @RequestBody UpdateWalletRequest request) {

        return walletService.save(null, onlineUser.getKeycloakUserId(), request.assets());
    }


}
