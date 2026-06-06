package com.exchange.wallet.controller.wallet;

import com.exchange.wallet.domain.AssetType;

import java.math.BigDecimal;

public record AssetDTO(BigDecimal balance,
                       BigDecimal blockedBalance,
                       AssetType assetType) {
}
