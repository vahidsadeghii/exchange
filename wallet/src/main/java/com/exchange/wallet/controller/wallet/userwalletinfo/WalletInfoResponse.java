package com.exchange.wallet.controller.wallet.userwalletinfo;

import com.exchange.wallet.domain.AssetType;

import java.math.BigDecimal;

public record WalletInfoResponse(String walletId, AssetType assetType, BigDecimal balance) {
}
