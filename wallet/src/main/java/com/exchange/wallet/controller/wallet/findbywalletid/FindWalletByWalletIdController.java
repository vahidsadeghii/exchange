package com.exchange.wallet.controller.wallet.findbywalletid;


import com.exchange.wallet.domain.Wallet;
import com.exchange.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class FindWalletByWalletIdController {
    private final WalletService walletService;


    @GetMapping(value = "${api.prefix.secure}/wallet-by-id")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Wallet handle(@RequestParam("walletId") String walletId) {

        return walletService.findWalletById(walletId);
    }
}
