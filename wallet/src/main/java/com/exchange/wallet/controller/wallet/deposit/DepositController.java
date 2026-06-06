package com.exchange.wallet.controller.wallet.deposit;


import com.exchange.wallet.config.security.OnlineUser;
import com.exchange.wallet.domain.Wallet;
import com.exchange.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DepositController {
    private final WalletService walletService;



    @PostMapping(value = "${api.prefix.secure}/deposit")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Wallet handle(@AuthenticationPrincipal OnlineUser onlineUser,
                          @RequestBody DepositRequest request) {
        Wallet wallet = walletService.depositWallet(request.walletId(), onlineUser.getKeycloakUserId(),
                request.assetType(), request.amount());
        return wallet;
    }

}
