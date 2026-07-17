package com.exchange.wallet.controller.wallet.unblock;

import com.exchange.wallet.domain.AssetType;
import com.exchange.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;


@RestController
@RequiredArgsConstructor
public class UnblockWalletAmountController {
    private final WalletService walletService;

    @GetMapping(value = "/_api/${api.version}/wallet-unblock")
    public void handle(@RequestParam Long userId, @RequestParam AssetType assetType, @RequestParam BigDecimal amount) {

        walletService.unblockWalletAmount(userId, assetType, amount);
    }
}
