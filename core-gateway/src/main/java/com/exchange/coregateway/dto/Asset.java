package com.exchange.coregateway.dto;

import com.exchange.wallet.sbe.AssetType;
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

}

