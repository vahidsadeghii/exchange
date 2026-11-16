package com.exchange.wallet.domain;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Wallet {
    private String walletId;
    private Long userId;
    private List<Asset> assets;

    private WalletStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
