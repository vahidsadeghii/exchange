package com.exchange.wallet.controller.wallet.deposit;

import com.exchange.wallet.domain.AssetType;

import java.math.BigDecimal;

public record DepositRequest(
        String walletId,
        AssetType assetType,
        BigDecimal amount) {
}
