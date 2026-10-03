package com.exchange.coregateway.controller.wallet;

import com.exchange.wallet.sbe.AssetType;

import java.math.BigDecimal;

public record WalletRequest(String walletId, AssetType type, BigDecimal amount){
}
