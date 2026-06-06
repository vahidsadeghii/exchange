package com.exchange.wallet.repository;

import com.exchange.wallet.domain.AssetType;
import com.exchange.wallet.domain.Wallet;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;


@Repository
public class InMemoryWalletRepository {

    private final Map<String, Wallet> walletMap = new ConcurrentHashMap<>();

    public Map<String, Wallet> save(Wallet wallet) {
        walletMap.put(wallet.getWalletId(), wallet);

        System.out.println(walletMap);

        return walletMap;
    }

    public Optional<Wallet> findById(String walletId) {
        return Optional.ofNullable(walletMap.get(walletId));
    }


    public Wallet findByUserId(Long userId) {
        return walletMap.values().stream()
                .filter(w -> w.getUserId().equals(userId))
                .findFirst()
                .orElse(null);
    }


    public void delete(String walletId) {
        walletMap.remove(walletId);
    }

    public Optional<Wallet> findByUserIdAndAssetType(Long userId, AssetType assetType) {
        return walletMap.values().stream()
                .filter(wallet -> wallet.getUserId().equals(userId))
                .filter(wallet -> containsAssetType(wallet, assetType))
                .findFirst();
    }

    private boolean containsAssetType(Wallet wallet, AssetType assetType) {
        return wallet.getAsserts() != null &&
                wallet.getAsserts().stream()
                        .anyMatch(asset -> asset.getAssetType() == assetType);
    }

}
