package com.exchange.wallet.service.serviceImpl;

import com.exchange.wallet.controller.wallet.AssetDTO;
import com.exchange.wallet.domain.*;
import com.exchange.wallet.exception.AssetNotFoundException;
import com.exchange.wallet.exception.WalletNotFoundException;
import com.exchange.wallet.service.TransactionService;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class WalletService implements AutoCloseable {
    private final WalletStore walletStore;
    private final TransactionService transactionService;
    private WalletClusterClient walletClusterClient;


    public synchronized Wallet save(Long userId, List<AssetDTO> assets) {
        if (walletClusterClient == null) {
            walletClusterClient = new WalletClusterClient();
        }
        return walletClusterClient.createWallet(userId, assets);
    }

    public Wallet updateWallet(String walletId, List<AssetDTO> assets) {
        Wallet wallet = findWalletById(walletId);

        Map<AssetType, BigDecimal> oldBalances = wallet.getAssets().stream()
                .collect(Collectors.toMap(Asset::getAssetType, Asset::getBalance));

        Map<AssetType, BigDecimal> oldBlockedBalances = wallet.getAssets().stream()
                .collect(Collectors.toMap(Asset::getAssetType, Asset::getBlockedBalance));

        mergeAssets(wallet, assets);

        wallet.setUpdatedAt(LocalDateTime.now());
        wallet.getAssets().forEach(asset -> {
            BigDecimal balanceBefore = oldBalances.getOrDefault(asset.getAssetType(), BigDecimal.ZERO);
            BigDecimal balanceAfter = asset.getBalance();
            BigDecimal blockedBefore = oldBlockedBalances.getOrDefault(asset.getAssetType(), BigDecimal.ZERO);

            BigDecimal blockedAfter = asset.getBlockedBalance();
            if (balanceBefore.compareTo(balanceAfter) == 0 &&
                    blockedBefore.compareTo(blockedAfter) == 0) {
                return;
            }
            TransactionType type;
            if (balanceAfter.compareTo(balanceBefore) > 0) {
                type = TransactionType.DEPOSIT;
            } else if (balanceAfter.compareTo(balanceBefore) < 0) {
                type = TransactionType.WITHDRAW;
            } else if (blockedAfter.compareTo(blockedBefore) > 0) {
                type = TransactionType.BLOCK;
            } else {
                type = TransactionType.UNBLOCK;
            }

            transactionService.createTransaction(
                    wallet.getUserId(),
                    wallet.getWalletId(),
                    asset.getAssetType(),
                    type,
                    balanceBefore,
                    balanceAfter,
                    blockedBefore,
                    blockedAfter
            );
        });

        walletStore.save(wallet);

        return wallet;
    }

    public Wallet depositWallet(String walletId, AssetType assetType, BigDecimal amount) {
        Wallet wallet = findWalletById(walletId);

        Asset asset = getAsset(wallet, assetType);

        BigDecimal balanceBefore = asset.getBalance();
        BigDecimal blockedBefore = asset.getBlockedBalance();

        asset.increase(amount);

        saveWallet(wallet);

        transactionService.createTransaction(
                wallet.getUserId(),
                wallet.getWalletId(),
                assetType,
                TransactionType.DEPOSIT,
                balanceBefore,
                asset.getBalance(),
                blockedBefore,
                asset.getBlockedBalance()
        );

        return wallet;
    }

    public Wallet withdrawWallet(String walletId, AssetType type, BigDecimal amount) {
        Wallet wallet = walletStore.findById(walletId)
                .orElseThrow(WalletNotFoundException::new);


        Asset asset = wallet.getAssets().stream()
                .filter(a -> a.getAssetType().equals(type))
                .findFirst()
                .orElseThrow(AssetNotFoundException::new);

        BigDecimal balanceBefore = asset.getBalance();
        BigDecimal blockedBalanceBefore = asset.getBlockedBalance();
        asset.withdraw(amount);

        BigDecimal balanceAfter = asset.getBalance();
        BigDecimal blockedBalanceAfter = asset.getBlockedBalance();

        wallet.setUpdatedAt(LocalDateTime.now());
        walletStore.save(wallet);

        transactionService.createTransaction(
                wallet.getUserId(),
                wallet.getWalletId(),
                asset.getAssetType(),
                TransactionType.WITHDRAW,

                balanceBefore,
                balanceAfter,

                blockedBalanceBefore,
                blockedBalanceAfter
        );
        return wallet;
    }

    public Wallet userWalletInfo(Long onlineUser, AssetType assetType) {
        return walletStore.findByUserIdAndAssetType(onlineUser, assetType)
                .orElseThrow(WalletNotFoundException::new);
    }

    public Wallet findWalletById(String walletId) {
        return walletStore.findById(walletId)
                .orElseThrow(WalletNotFoundException::new);
    }

    public BigDecimal findBalanceByUserId(Long onlineUser, AssetType assetType) {
        Wallet wallet = findWalletByUserIdAndAssetType(onlineUser, assetType)
                .orElseThrow(WalletNotFoundException::new);

        return wallet.getAssets()
                .stream()
                .filter(asset -> asset.getAssetType() == assetType)
                .findFirst()
                .map(Asset::getBalance)
                .orElse(BigDecimal.ZERO);
    }

    public void blockWalletAmount(Long userId, AssetType assetType, BigDecimal amount) {
        Wallet wallet = findWalletByUserIdAndAssetType(userId, assetType)
                .orElseThrow(WalletNotFoundException::new);

        Asset asset = getAsset(wallet, assetType);

        BigDecimal balanceBefore = asset.getBalance();
        BigDecimal blockedBefore = asset.getBlockedBalance();

        // Move amount to blocked balance
        asset.block(amount);

        BigDecimal balanceAfter = asset.getBalance();
        BigDecimal blockedAfter = asset.getBlockedBalance();

        wallet.setUpdatedAt(LocalDateTime.now());
        walletStore.save(wallet);

        transactionService.createTransaction(
                wallet.getUserId(),
                wallet.getWalletId(),
                assetType,
                TransactionType.BLOCK,

                balanceBefore,
                balanceAfter,

                blockedBefore,
                blockedAfter
        );
    }

    public void consumeBlockedAmount(Long userId, AssetType assetType, BigDecimal amount) {
        Wallet wallet = findWalletByUserIdAndAssetType(userId, assetType)
                .orElseThrow(WalletNotFoundException::new);

        Asset asset = getAsset(wallet, assetType);

        BigDecimal balanceBefore = asset.getBalance();
        BigDecimal blockedBefore = asset.getBlockedBalance();

        asset.consumeBlocked(amount);

        saveWallet(wallet);


        transactionService.createTransaction(
                wallet.getUserId(),
                wallet.getWalletId(),
                assetType,
                TransactionType.WITHDRAW,
                balanceBefore,
                asset.getBalance(),
                blockedBefore,
                asset.getBlockedBalance()
        );
    }

    public void unblockWalletAmount(Long userId, AssetType assetType, BigDecimal amount) {

        Wallet wallet = findWalletByUserIdAndAssetType(userId, assetType)
                .orElseThrow(WalletNotFoundException::new);
        Asset asset = getAsset(wallet, assetType);

        BigDecimal balanceBefore = asset.getBalance();
        BigDecimal blockedBefore = asset.getBlockedBalance();

        asset.unblock(amount);

        saveWallet(wallet);

        transactionService.createTransaction(
                wallet.getUserId(),
                wallet.getWalletId(),
                assetType,
                TransactionType.UNBLOCK,
                balanceBefore,
                asset.getBalance(),
                blockedBefore,
                asset.getBlockedBalance()
        );
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

    private void mergeAssets(Wallet wallet, List<AssetDTO> assets) {
        List<Asset> newAssets = assetEntities(assets);
        if (wallet.getAssets() == null) {
            wallet.setAssets(new ArrayList<>());
        } else if (!(wallet.getAssets() instanceof ArrayList)) {
            wallet.setAssets(new ArrayList<>(wallet.getAssets()));
        }

        Map<AssetType, Asset> existingAssets =
                wallet.getAssets()
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
                wallet.getAssets().add(newAsset);
            }
        }
    }


    private Asset getAsset(Wallet wallet, AssetType assetType) {

        return wallet.getAssets()
                .stream()
                .filter(asset -> asset.getAssetType() == assetType)
                .findFirst()
                .orElseThrow(AssetNotFoundException::new);
    }

    private Wallet saveWallet(Wallet wallet) {
        wallet.setUpdatedAt(LocalDateTime.now());
        return walletStore.save(wallet);
    }

    private List<Wallet> findWalletByUserId(Long userId) {

        return walletStore.findByUserId(userId);
    }

    private Optional<Wallet> findWalletByUserIdAndAssetType(Long userId, AssetType assetType) {

        return walletStore.findByUserIdAndAssetType(userId, assetType);
    }

    @Override
    public synchronized void close() {
        if (walletClusterClient != null) {
            walletClusterClient.close();
            walletClusterClient = null;
        }
    }
}
