package com.exchange.wallet.controller.wallet.withdrawwallet;

import com.exchange.wallet.domain.AssetType;

import java.math.BigDecimal;

public record WithdrawWalletRequest (String walletId, AssetType type, BigDecimal amount){
}
