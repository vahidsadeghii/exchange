package com.exchange.wallet.service.serviceImpl;

import com.exchange.wallet.client.profileclient.ProfileClient;
import com.exchange.wallet.controller.wallet.AssetDTO;
import com.exchange.wallet.domain.Asset;
import com.exchange.wallet.domain.AssetType;
import com.exchange.wallet.domain.Wallet;
import com.exchange.wallet.domain.WalletStatus;
import com.exchange.wallet.exception.AmountMustBePositiveException;
import com.exchange.wallet.exception.AssetNotFoundException;
import com.exchange.wallet.exception.UserCanNotFoundException;
import com.exchange.wallet.exception.WalletNotFoundException;
import com.exchange.wallet.repository.InMemoryWalletRepository;
import com.exchange.wallet.service.WalletService;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {
    private final InMemoryWalletRepository repository;
    private final ProfileClient profileClient;


    @Override
    public Map<String, Wallet> save(Long userId, String keycloakId, List<AssetDTO> assets) {
        List<Asset> assetEntities = assetEntities(assets);

        //Create Wallet
        if (userId != null) {
            return repository.save(Wallet.builder()
                    .walletId(UUID.randomUUID().toString())
                    .userId(userId)
                    .asserts(assetEntities)
                    .status(WalletStatus.ACTIVE)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build());
        }

        //Update Wallet
        if (StringUtils.isEmpty(keycloakId)) {
            throw new RuntimeException("keycloakId is required for wallet update");
        }

        Long resolvedUserId = getUserIdByKeycloakId(keycloakId)
                .orElseThrow(UserCanNotFoundException::new);
        Wallet existingWallet = repository.findByUserId(resolvedUserId);

        if (existingWallet == null) {
            throw new WalletNotFoundException();
        }

        mergeAssets(existingWallet, assetEntities);

        existingWallet.setUpdatedAt(LocalDateTime.now());
        return repository.save(existingWallet);
    }


    @Override
    public Wallet increaseWallet(String walletId, String onlineUserId, AssetType assetType, BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new AmountMustBePositiveException();
        }

        Wallet wallet = repository.findById(walletId)
                .orElseThrow(WalletNotFoundException::new);

        Asset asset = wallet.getAsserts().stream()
                .filter(a -> a.getAssetType() == assetType)
                .findFirst()
                .orElseThrow(AssetNotFoundException::new);

        asset.increase(amount);
        wallet.setUpdatedAt(LocalDateTime.now());

        repository.save(wallet);
        return wallet;
    }

    @Override
    public Wallet userWalletInfo(String keycloakId, AssetType assetType) {
        Long userId = getUserIdByKeycloakId(keycloakId)
                .orElseThrow(UserCanNotFoundException::new);

        return repository
                .findByUserIdAndAssetType(userId, assetType)
                .orElse(null);
    }

    public Map<String, Wallet> withdraw(String walletId, AssetType type, BigDecimal amount) {

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

    private List<Asset> assetEntities(List<AssetDTO> assets) {
        return assets.stream()
                .map(dto -> Asset.builder()
                        .balance(dto.balance())
                        .blockedBalance(dto.blockedBalance())
                        .assetType(dto.assetType())
                        .build())
                .toList();
    }

    private Optional<Long> getUserIdByKeycloakId(String keycloakId) {
        return profileClient.findUserByKeycloakId(keycloakId);
    }

    private void mergeAssets(Wallet wallet, List<Asset> newAssets) {

    if (wallet.getAsserts() == null) {
        wallet.setAsserts(new ArrayList<>());
    } else if (!(wallet.getAsserts() instanceof ArrayList)) {
        wallet.setAsserts(new ArrayList<>(wallet.getAsserts()));
    }

    Map<AssetType, Asset> existingAssets =
            wallet.getAsserts()
                    .stream()
                    .collect(Collectors.toMap(
                            Asset::getAssetType,
                            asset -> asset,
                            (a, b) -> a
                    ));

    for (Asset newAsset : newAssets) {

        Asset existing = existingAssets.get(newAsset.getAssetType());

        if (existing != null) {
            existing.setBalance(newAsset.getBalance());
            existing.setBlockedBalance(newAsset.getBlockedBalance());
        } else {
            wallet.getAsserts().add(newAsset); // ✅ now safe
        }
    }
}
}
