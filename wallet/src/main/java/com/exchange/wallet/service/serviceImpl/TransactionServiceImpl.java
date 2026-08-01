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
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {
    private final InMemoryTransactionInfoRepository inMemoryTransactionInfoRepository;


    @Override
    public void createTransaction(Long userId, String walletId, AssetType assetType,
                                  TransactionType type,
                                  BigDecimal balanceBefore, BigDecimal balanceAfter,
                                  BigDecimal blockedBalanceBefore, BigDecimal blockedBalanceAfter) {

        BigDecimal amount = switch (type) {

            case DEPOSIT,
                 REFUND,
                 ADJUSTMENT -> balanceAfter.subtract(balanceBefore).abs();

            case WITHDRAW,
                 TRANSFER -> balanceBefore.subtract(balanceAfter).abs();

            case BLOCK -> blockedBalanceAfter.subtract(blockedBalanceBefore).abs();

            case UNBLOCK -> blockedBalanceBefore.subtract(blockedBalanceAfter).abs();
        };

        TransactionInfo transaction = TransactionInfo.builder()
                .id(UUID.randomUUID().toString())
                .walletId(walletId)
                .userId(userId)
                .assetType(assetType)
                .status(TransactionStatus.SUCCESS)
                .amount(amount)
                .type(type)

                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)

                .blockedBalanceBefore(blockedBalanceBefore)
                .blockedBalanceAfter(blockedBalanceAfter)

                .createdAt(LocalDateTime.now())
                .build();


        inMemoryTransactionInfoRepository.save(transaction);

    }

    @Override
    public List<TransactionInfo> findTransactions(String walletId) {
        List<TransactionInfo> allByWalletId = inMemoryTransactionInfoRepository.findAllByWalletId(walletId);
        return allByWalletId;
    }
}
