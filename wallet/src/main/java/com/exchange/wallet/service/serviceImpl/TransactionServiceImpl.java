package com.exchange.wallet.service.serviceImpl;

import com.exchange.wallet.domain.TransactionInfo;
import com.exchange.wallet.domain.TransactionType;
import com.exchange.wallet.repository.InMemoryTransactionInfoRepository;
import com.exchange.wallet.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;


@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {
    private final InMemoryTransactionInfoRepository inMemoryTransactionInfoRepository;


    @Override
    public TransactionInfo createTransaction(String walletId, long userId, TransactionType type, BigDecimal amount) {
        List<TransactionInfo> transactionInfos = inMemoryTransactionInfoRepository.findAllByWalletId(walletId);
        TransactionInfo transactionInfo = new TransactionInfo();
        return null;
    }
}
