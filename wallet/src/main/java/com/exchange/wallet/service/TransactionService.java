package com.exchange.wallet.service;

import com.exchange.wallet.domain.TransactionInfo;
import com.exchange.wallet.domain.TransactionType;

import java.math.BigDecimal;


public interface TransactionService {

    TransactionInfo createTransaction(
            String walletId, long userId,
            TransactionType type, BigDecimal amount);
}
