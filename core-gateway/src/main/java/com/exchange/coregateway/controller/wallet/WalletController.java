package com.exchange.coregateway.controller.wallet;




import com.exchange.coregateway.service.WalletService;
import com.exchange.coresdk.domain.WalletInfoResponse;
import com.exchange.coresdk.domain.WalletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
public class WalletController {
        private final WalletService walletService;

    @PostMapping(value = "${api.prefix.secure}/withdraw")
    //@PreAuthorize("hasRole('CUSTOMER')")
    public WalletResponse withdrawWallet(@RequestBody WalletRequest request) {
        return walletService.withdrawWallet(request.walletId(), request.type(), request.amount());
    }

   @PostMapping(value = "${api.prefix.internal}/user/wallet")
    public WalletInfoResponse createWallet(@RequestParam("userId") Long userId) {
         walletService.save(userId, null);

       return null;
    }
//
//
//    @PostMapping(value = "${api.prefix.secure}/deposit")
//    public Wallet handle(@AuthenticationPrincipal OnlineUser onlineUser,
//                         @RequestBody DepositRequest request) {
//        Wallet wallet = walletService.depositWallet(request.walletId(),
//                request.assetType(), request.amount());
//        return wallet;
//    }
//
//
//    @GetMapping(value = "${api.prefix.secure}/wallet-by-id")
//    public Wallet handle(@RequestParam("walletId") String walletId) {
//
//        return walletService.findWalletById(walletId);
//    }
//
//    @GetMapping(value = "/_api/${api.version}/wallet-unblock")
//    public void handle(@RequestParam Long userId, @RequestParam AssetType assetType, @RequestParam BigDecimal amount) {
//
//        walletService.unblockWalletAmount(userId, assetType, amount);
//    }
//
//    @PostMapping(value = "${api.prefix.secure}/user/wallet")
//    public Wallet handle(@AuthenticationPrincipal OnlineUser onlineUser,
//                         @RequestBody UpdateWalletRequest request) {
//
//        return walletService.save(onlineUser.getInternalUserId(), request.assets());
//    }
//
//    @GetMapping(value = "/_api/${api.version}/wallet-balance")
//    public BigDecimal handle(@RequestParam Long userId, @RequestParam AssetType assetType) {
//
//        return walletService.findBalanceByUserId(userId, assetType);
//    }
//
//    @GetMapping(value = "${api.prefix.secure}/wallet-info")
//    public WalletInfoResponse handle(@RequestParam("assetType") AssetType assetType,
//                                     @AuthenticationPrincipal OnlineUser onlineUser) {
//        Wallet wallet = walletService.userWalletInfo(onlineUser.getInternalUserId(), assetType);
//
//        Asset asset = wallet.getAssets().stream()
//                .filter(a -> a.getAssetType() == assetType)
//                .findFirst()
//                .orElseThrow(WalletNotFoundException::new);
//
//        return new WalletInfoResponse(
//                wallet.getWalletId(),
//                asset.getAssetType(),
//                asset.getBalance()
//        );
//    }
//
//    @PostMapping(value = "${api.prefix.secure}/withdraw")
//    @PreAuthorize("hasRole('CUSTOMER')")
//    public Wallet handle(@RequestBody WithdrawWalletRequest request) {
//        return walletService.withdrawWallet(request.walletId(), request.type(), request.amount());
//    }
}
