package com.exchange.wallet.controller.wallet.createwallet;


import com.exchange.wallet.controller.wallet.AssetDTO;
import com.exchange.wallet.domain.AssetType;
import com.exchange.wallet.domain.Wallet;
import com.exchange.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class CreateWalletController {
    private final WalletService walletService;

    @PostMapping(value = "${api.prefix.internal}/user/wallet")
    public Wallet handle(@RequestParam("userId") Long userId) {
        return walletService.save(userId, null, List.of(new AssetDTO(BigDecimal.ZERO, BigDecimal.ZERO, AssetType.EUR)));
    }
}
