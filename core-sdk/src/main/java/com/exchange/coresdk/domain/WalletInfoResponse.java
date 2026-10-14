package com.exchange.coresdk.domain;



import com.exchange.wallet.sbe.AssetType;

import java.math.BigDecimal;

public record WalletInfoResponse(String walletId, AssetType assetType, BigDecimal balance) {
}
