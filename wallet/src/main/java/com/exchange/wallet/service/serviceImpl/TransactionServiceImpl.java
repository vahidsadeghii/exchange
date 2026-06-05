package com.exchange.wallet.service.serviceImpl;

import com.exchange.wallet.domain.AssetType;
import com.exchange.wallet.domain.TransactionInfo;
import com.exchange.wallet.domain.TransactionStatus;
import com.exchange.wallet.domain.TransactionType;
import com.exchange.wallet.repository.InMemoryTransactionInfoRepository;
import com.exchange.wallet.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {
    private final InMemoryTransactionInfoRepository inMemoryTransactionInfoRepository;


    @Override
    public void createTransaction(String walletId, long userId, AssetType assetType,
                                  BigDecimal balanceBefore, BigDecimal balanceAfter,
                                  TransactionType type) {
        var amount = switch (type) {
            case DEPOSIT,
                 UNBLOCK,
                 REFUND -> balanceAfter.subtract(balanceBefore);

            case WITHDRAW,
                 BLOCK -> balanceBefore.subtract(balanceAfter);

            case TRANSFER -> balanceBefore.subtract(balanceAfter);

            case ADJUSTMENT -> balanceAfter.subtract(balanceBefore);
        };

        inMemoryTransactionInfoRepository.save(TransactionInfo.builder()
                .id(UUID.randomUUID().toString())
                .walletId(walletId)
                .userId(userId)
                .assetType(assetType)
                .type(type)
                .status(TransactionStatus.SUCCESS)
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .amount(amount)
                .createdAt(LocalDateTime.now())
                .build());

    }
}
