package com.exchange.wallet.controller.wallet.createwallet;


import com.exchange.wallet.domain.Wallet;
import com.exchange.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CreateWalletController {
    private final WalletService walletService;


    //@PreAuthorize("hasRole('CUSTOMER')")
    //public Wallet handle(@AuthenticationPrincipal OnlineUser onlineUser,


    @PostMapping(value = "${api.prefix.open}/user/create-wallet")
    public Wallet handle(@RequestBody CreateWalletRequest request) {

        return walletService.save("30eebbc1-b565-457c-9f74-8c5c1213e235", request.assets());
    }


}
