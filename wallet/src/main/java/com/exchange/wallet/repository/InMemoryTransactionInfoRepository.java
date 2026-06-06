package com.exchange.wallet.repository;

import com.exchange.wallet.domain.TransactionInfo;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;


@Repository
public class InMemoryTransactionInfoRepository {

    private final Map<String, TransactionInfo> transactionInfotMap = new ConcurrentHashMap<>();

    public TransactionInfo save(TransactionInfo transactionInfo) {
        transactionInfotMap.put(transactionInfo.getId(), transactionInfo);
        return transactionInfo;
    }

    public Optional<TransactionInfo> findById(String transactionInfoId) {
        return Optional.ofNullable(transactionInfotMap.get(transactionInfoId));
    }

    public List<TransactionInfo> findAllByWalletId(String walletId) {
        return transactionInfotMap.values()
                .stream()
                .filter(transaction -> walletId.equals(transaction.getWalletId()))
                .toList();
    }
}
