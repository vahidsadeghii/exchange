package com.exchange.coregateway.service;

import com.exchange.coresdk.Client;
import com.exchange.coresdk.domain.WalletResponse;
import com.exchange.wallet.sbe.AssetType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletService {
    private final Client client;


    public WalletResponse withdrawWallet(String walletId, AssetType type, BigDecimal amount) {
        try {
            return client.withdrawWallet(walletId, type, amount).get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Error while fetching wallet {}, {}", walletId, type, e);
            return null;
        }
    }

}
