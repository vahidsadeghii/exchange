package com.exchange.wallet.domain;


import com.exchange.wallet.exception.AmountMustBePositiveException;
import com.exchange.wallet.exception.InsufficientBalanceException;
import com.exchange.wallet.exception.InsufficientBlockedBalanceException;
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


   /**
     * Returns the amount that can be used.
     */
    public BigDecimal getAvailableBalance() {
        return balance.subtract(blockedBalance);
    }


    /**
     * Increase wallet balance.
     */
    public void increase(BigDecimal amount) {
        validateAmount(amount);

        balance = balance.add(amount);
    }


    /**
     * Withdraw from available balance.
     */
    public void withdraw(BigDecimal amount) {
        validateAmount(amount);

        if (getAvailableBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException();
        }

        balance = balance.subtract(amount);
    }


    /**
     * Move balance to blocked balance.
     * Balance remains unchanged.
     */
    public void block(BigDecimal amount) {
        validateAmount(amount);

        if (getAvailableBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException();
        }

        blockedBalance = blockedBalance.add(amount);
    }


    /**
     * Release blocked balance back to available balance.
     */
    public void unblock(BigDecimal amount) {
        validateAmount(amount);

        if (blockedBalance.compareTo(amount) < 0) {
            throw new InsufficientBlockedBalanceException();
        }

        blockedBalance = blockedBalance.subtract(amount);
    }


    /**
     * Consume blocked balance.
     * Used when a blocked amount is finally spent.
     */
    public void consumeBlocked(BigDecimal amount) {
        validateAmount(amount);

        if (blockedBalance.compareTo(amount) < 0) {
            throw new InsufficientBlockedBalanceException();
        }

        blockedBalance = blockedBalance.subtract(amount);
        balance = balance.subtract(amount);
    }


    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new AmountMustBePositiveException();
        }
    }
}

