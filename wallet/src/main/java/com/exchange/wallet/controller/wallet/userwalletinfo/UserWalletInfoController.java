package com.exchange.wallet.controller.wallet.userwalletinfo;


import com.exchange.wallet.config.security.OnlineUser;
import com.exchange.wallet.domain.Asset;
import com.exchange.wallet.domain.AssetType;
import com.exchange.wallet.domain.Wallet;
import com.exchange.wallet.exception.WalletNotFoundException;
import com.exchange.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserWalletInfoController {
    private final WalletService walletService;

    @GetMapping(value = "${api.prefix.secure}/wallet-info")
    @PreAuthorize("hasRole('CUSTOMER')")
    public WalletInfoResponse handle(@RequestParam("assetType") AssetType assetType,
                                     @AuthenticationPrincipal OnlineUser onlineUser) {
        Wallet wallet = walletService.userWalletInfo(onlineUser.getKeycloakUserId(), assetType);

        if (wallet == null) {
            throw new WalletNotFoundException();
        }

        Asset asset = wallet.getAsserts().stream()
                .filter(a -> a.getAssetType() == assetType)
                .findFirst()
                .orElseThrow(WalletNotFoundException::new);

        return new WalletInfoResponse(
                wallet.getWalletId(),
                asset.getAssetType(),
                asset.getBalance()
        );
    }
}
