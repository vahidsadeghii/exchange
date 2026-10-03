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

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public BigDecimal getBlockedBalance() {
        return blockedBalance;
    }

    public void setBlockedBalance(BigDecimal blockedBalance) {
        this.blockedBalance = blockedBalance;
    }

    public AssetType getAssetType() {
        return assetType;
    }

    public void setAssetType(AssetType assetType) {
        this.assetType = assetType;
    }
}
