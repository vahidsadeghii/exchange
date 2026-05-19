package com.exchange.wallet.domain;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionInfo {
    private String id;
    private Long userId;
    private String walletId;
    private AssetType assetType;
    private BigDecimal amount;

    private TransactionType type;

    private TransactionStatus status;

    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;

    private LocalDateTime createdAt;
}
