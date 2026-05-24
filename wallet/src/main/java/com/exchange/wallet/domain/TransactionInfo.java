package com.exchange.wallet.domain;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

    @Enumerated(EnumType.STRING)
    private AssetType assetType;
    @Enumerated(EnumType.STRING)
    private TransactionStatus status;
    private BigDecimal amount;
    private TransactionType type;

    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;

    private LocalDateTime createdAt;
}
