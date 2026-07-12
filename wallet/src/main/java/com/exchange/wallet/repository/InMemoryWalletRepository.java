package com.exchange.wallet.repository;

import com.exchange.wallet.domain.AssetType;
import com.exchange.wallet.domain.Wallet;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;


@Repository
public class InMemoryWalletRepository {

    private final Map<String, Wallet> walletMap = new ConcurrentHashMap<>();

    public Wallet save(Wallet wallet) {
        walletMap.put(wallet.getWalletId(), wallet);

        return wallet;
    }

    public Optional<Wallet> findById(String walletId) {
        return Optional.ofNullable(
                walletMap.get(walletId)
        );
    }

    public Optional<Wallet> findByUserIdAndAssetType(Long userId, AssetType assetType) {
        return walletMap.values()
                .stream()
                .filter(wallet -> wallet.getUserId().equals(userId))
                .filter(wallet -> wallet.getAssets()
                        .stream()
                        .anyMatch(asset -> asset.getAssetType() == assetType)
                )
                .findFirst();
    }

    public Optional<Wallet> findByUserId(Long userId) {
        return walletMap.values()
                .stream()
                .filter(wallet -> wallet.getUserId().equals(userId))
                .findFirst();
    }

    public void delete(String walletId) {
        walletMap.remove(walletId);
    }
}