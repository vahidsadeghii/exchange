package com.exchange.wallet.service;

import com.exchange.wallet.controller.wallet.AssetDTO;
import com.exchange.wallet.domain.AssetType;
import com.exchange.wallet.domain.Wallet;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface WalletService {


    Map<String, Wallet> save(Long userId, String keycloakId, List<AssetDTO> assets);


    Wallet increaseWallet(String walletId, String onlineUserId, AssetType assetType, BigDecimal amount);

    Wallet userWalletInfo(String keycloakId, AssetType assetType);
}
