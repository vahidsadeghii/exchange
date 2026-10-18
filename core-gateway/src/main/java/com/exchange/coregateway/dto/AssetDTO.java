package com.exchange.coregateway.dto;



import com.exchange.wallet.sbe.AssetType;

import java.math.BigDecimal;

public record AssetDTO(BigDecimal balance,
                       BigDecimal blockedBalance,
                       AssetType assetType) {
}
