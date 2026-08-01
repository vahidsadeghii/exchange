package com.exchange.wallet.service;

import com.exchange.wallet.domain.AssetType;
import com.exchange.wallet.domain.TransactionInfo;
import com.exchange.wallet.domain.TransactionType;

import java.math.BigDecimal;
import java.util.List;


public interface TransactionService {

    void createTransaction(  Long userId,
            String walletId,
            AssetType assetType,
            TransactionType type,
            BigDecimal balanceBefore,
            BigDecimal balanceAfter,
            BigDecimal blockedBalanceBefore,
            BigDecimal blockedBalanceAfter);

    List<TransactionInfo> findTransactions(String walletId);
}
