package com.exchange.wallet.service;

import com.exchange.wallet.controller.wallet.createwallet.AssetDTO;
import com.exchange.wallet.domain.Asset;
import com.exchange.wallet.domain.AssetType;
import com.exchange.wallet.domain.Wallet;

import java.math.BigDecimal;
import java.util.List;

public interface WalletService {

    Wallet save(String keycloakId, List<AssetDTO> assets);

    Wallet deposit(String walletId, String onlineUser, AssetType assetType, BigDecimal amount);
}
