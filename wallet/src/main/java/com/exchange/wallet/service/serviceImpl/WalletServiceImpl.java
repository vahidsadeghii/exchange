package com.exchange.wallet.service.serviceImpl;

import com.exchange.wallet.domain.Asset;
import com.exchange.wallet.domain.AssetType;
import com.exchange.wallet.domain.Wallet;
import com.exchange.wallet.domain.WalletStatus;
import com.exchange.wallet.exception.AmountMustBePositiveException;
import com.exchange.wallet.exception.AssetNotFoundException;
import com.exchange.wallet.exception.WalletNotFoundException;
import com.exchange.wallet.repository.InMemoryWalletRepository;
import com.exchange.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {
    private final InMemoryWalletRepository repository;


    @Override
    public Wallet save(String keycloakId, List<Asset> assets) {
        Long userId = getUserIdByKeycloakId(keycloakId);

        return repository.save(Wallet.builder()
                .walletId(UUID.randomUUID().toString())
                .userId(userId)
                .asserts(assets)
                .status(WalletStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());
    }

    @Override
    public Wallet deposit(String walletId, String onlineUser, AssetType assetType, BigDecimal amount) {

        if (amount == null || amount.signum() <= 0) {
            throw new AmountMustBePositiveException();
        }

        Wallet wallet = repository.findById(walletId)
                .orElseThrow(WalletNotFoundException::new);

        Asset asset = wallet.getAsserts().stream()
                .filter(a -> a.getAssetType() == assetType)
                .findFirst()
                .orElseThrow(AssetNotFoundException::new);

        asset.deposit(amount);
        wallet.setUpdatedAt(LocalDateTime.now());

        repository.save(wallet);

        return wallet;
    }

    public Wallet withdraw(String walletId, AssetType type, BigDecimal amount) {

        Wallet wallet = repository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        Asset asset = wallet.getAsserts().stream()
                .filter(a -> a.getAssetType() == type)
                .findFirst()
                .orElseThrow(AssetNotFoundException::new);

        asset.withdraw(amount);

        wallet.setUpdatedAt(LocalDateTime.now());

        return repository.save(wallet);
    }


    private Long getUserIdByKeycloakId(String keycloakId) {
        return 100L;
    }
}
