package com.exchange.wallet.repository;

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


    public Wallet save(Wallet wallet) {
        walletMap.put(wallet.getWalletId(), wallet);
        return wallet;
    }

    public Optional<Wallet> findById(String walletId) {
        return Optional.ofNullable(walletMap.get(walletId));
    }

    public List<Wallet> findAll() {
        return new ArrayList<>(walletMap.values());
    }

    public List<Wallet> findByUserId(Long userId) {
        return walletMap.values().stream()
                .filter(w -> w.getUserId().equals(userId))
                .toList();
    }

    public void delete(String walletId) {
        walletMap.remove(walletId);
    }
}
