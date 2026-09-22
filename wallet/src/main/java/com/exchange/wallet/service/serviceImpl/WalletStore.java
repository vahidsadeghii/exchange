package com.exchange.wallet.service.serviceImpl;

import com.exchange.wallet.domain.AssetType;
import com.exchange.wallet.domain.Wallet;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WalletStore {

    private final Map<String, Wallet> wallets = new ConcurrentHashMap<>();

    public Wallet save(Wallet wallet) {
        wallets.put(wallet.getWalletId(), wallet);
        return wallet;
    }

    public Optional<Wallet> findById(String walletId) {
        return Optional.ofNullable(wallets.get(walletId));
    }

    public List<Wallet> findByUserId(Long userId) {
        return wallets.values()
                .stream()
                .filter(wallet -> wallet.getUserId().equals(userId))
                .toList();
    }

public Optional<Wallet> findByUserIdAndAssetType(Long userId, AssetType assetType) {
    return wallets.values()
            .stream()
            .filter(wallet -> Objects.equals(wallet.getUserId(), userId))
            .filter(wallet -> wallet.getAssets().stream()
                    .anyMatch(asset -> asset.getAssetType() == assetType))
            .findFirst();
}

    public void delete(String walletId) {
        wallets.remove(walletId);
    }

    public void clear() {
        wallets.clear();
    }
}
