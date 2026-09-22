package com.exchange.coregateway.controller.wallet;




import com.exchange.coregateway.service.WalletService;
import com.exchange.coresdk.domain.WalletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class WalletController {
        private final WalletService walletService;

    @PostMapping(value = "${api.prefix.secure}/withdraw")
    //@PreAuthorize("hasRole('CUSTOMER')")
    public WalletResponse withdrawWallet(@RequestBody WalletRequest request) {
        return walletService.withdrawWallet(request.walletId(), request.type(), request.amount());
    }
}
