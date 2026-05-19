package com.exchange.wallet.controller.wallet.createwallet;


import com.exchange.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CreateWalletController {
    private final WalletService walletService;


}
