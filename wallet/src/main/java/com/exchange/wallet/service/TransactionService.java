package com.exchange.wallet.service;

import com.exchange.wallet.domain.AssetType;
import com.exchange.wallet.domain.TransactionInfo;
import com.exchange.wallet.domain.TransactionType;

import java.math.BigDecimal;


public interface TransactionService {

    void createTransaction(String walletId,
                                      long userId,
                                      AssetType assetType,
                                      BigDecimal balanceBefore,
                                      BigDecimal balanceAfter,
                                      TransactionType type);
}
