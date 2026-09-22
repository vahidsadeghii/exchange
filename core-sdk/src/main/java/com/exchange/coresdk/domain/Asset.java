package com.exchange.coresdk.domain;

import com.exchange.wallet.sbe.AssetType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.math.BigDecimal;

public class Asset {
    private BigDecimal balance = BigDecimal.ZERO;
    private BigDecimal blockedBalance = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    private AssetType assetType;
}
