package com.exchange.wallet.domain;


import com.exchange.wallet.exception.AmountMustBePositiveException;
import com.exchange.wallet.exception.InsufficientBalanceException;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

import java.math.BigDecimal;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Asset {
    private BigDecimal balance = BigDecimal.ZERO;
    private BigDecimal blockedBalance = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    private AssetType assetType;


    public void deposit(BigDecimal amount) {
        this.balance = this.balance.add(amount);
    }

    public void withdraw(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new AmountMustBePositiveException();
        }

        if (balance.compareTo(amount) < 0) {
            throw new InsufficientBalanceException();
        }

        balance = balance.subtract(amount);
    }
}
