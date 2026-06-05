package com.exchange.wallet.controller.wallet.withdrawwallet;


import com.exchange.wallet.domain.AssetType;
import com.exchange.wallet.domain.Wallet;
import com.exchange.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
public class WithdrawWalletController {
    private final WalletService walletService;

    @PostMapping(value = "${api.prefix.secure}/withdraw")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Wallet withdrawWallet(String walletId, AssetType type, BigDecimal amount) {
        return walletService.withdrawWallet(walletId, type, amount);
    }
}
