package com.exchange.wallet.service;

import com.exchange.wallet.controller.wallet.AssetDTO;
import com.exchange.wallet.domain.AssetType;
import com.exchange.wallet.domain.Wallet;

import java.math.BigDecimal;
import java.util.List;

public interface WalletService {


    Wallet save(Long userId, List<AssetDTO> assets);

    Wallet withdrawWallet(String walletId, AssetType type, BigDecimal amount);

    Wallet depositWallet(String walletId,  AssetType assetType, BigDecimal amount);

    Wallet userWalletInfo(Long onlineUser, AssetType assetType);

    Wallet findWalletById(String walletId);

    BigDecimal findBalanceByUserId(Long onlineUser, AssetType assetType);




}
