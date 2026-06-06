package com.exchange.wallet.domain;

public enum TransactionType {

    // Add balance to wallet
    DEPOSIT,

    // Withdraw balance from wallet
    WITHDRAW,

    // Transfer balance between wallets
    TRANSFER,

    // Move balance to blocked balance
    BLOCK,

    // Release blocked balance back to available balance
    UNBLOCK,

    // Reverse a previous transaction
    REFUND,

    // Manual balance adjustment by admin/system
    ADJUSTMENT
}

