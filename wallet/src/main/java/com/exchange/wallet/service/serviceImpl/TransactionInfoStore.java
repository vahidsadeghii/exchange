package com.exchange.wallet.service.serviceImpl;


import com.exchange.wallet.domain.TransactionInfo;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TransactionInfoStore {

    private final Map<String, TransactionInfo> transactions =
            new ConcurrentHashMap<>();

    public TransactionInfo save(TransactionInfo transactionInfo) {
        transactions.put(transactionInfo.getId(), transactionInfo);
        return transactionInfo;
    }

    public Optional<TransactionInfo> findById(String transactionId) {
        return Optional.ofNullable(transactions.get(transactionId));
    }

    public List<TransactionInfo> findAllByWalletId(String walletId) {
        return transactions.values()
                .stream()
                .filter(transaction ->
                        walletId.equals(transaction.getWalletId()))
                .toList();
    }

    public void delete(String transactionId) {
        transactions.remove(transactionId);
    }

    public void clear() {
        transactions.clear();
    }
}

