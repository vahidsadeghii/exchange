package com.exchange.wallet.service.serviceImpl;

import com.exchange.wallet.domain.Asset;
import com.exchange.wallet.domain.AssetType;
import com.exchange.wallet.domain.Wallet;
import com.exchange.wallet.repository.InMemoryWalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


@Component
@RequiredArgsConstructor
@Slf4j
public class WalletInitializer implements CommandLineRunner {

    private final InMemoryWalletRepository repository;

    @Override
    public void run(String... args) {

        if (repository.findByUserId(111L) == null) {

            List<Asset> assets = List.of(
                    Asset.builder()
                            .assetType(AssetType.BTC)
                            .balance(BigDecimal.valueOf(800000))
                            .blockedBalance(BigDecimal.ZERO)
                            .build(),

                    Asset.builder()
                            .assetType(AssetType.EUR)
                            .balance(BigDecimal.valueOf(5000))
                            .blockedBalance(BigDecimal.ZERO)
                            .build(),

                    Asset.builder()
                            .assetType(AssetType.USDT)
                            .balance(BigDecimal.valueOf(1000000))
                            .blockedBalance(BigDecimal.ZERO)
                            .build()
            );

            Wallet wallet = Wallet.builder()
                    .walletId(UUID.randomUUID().toString())
                    .userId(111L)
                    .assets(assets)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            repository.save(wallet);

            log.info("Default wallet created for user 111");
        }
    }
}
