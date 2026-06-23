package com.exchange.wallet.service.serviceImpl;

import com.exchange.wallet.client.profileclient.ProfileClient;
import com.exchange.wallet.controller.wallet.AssetDTO;
import com.exchange.wallet.domain.*;
import com.exchange.wallet.exception.AmountMustBePositiveException;
import com.exchange.wallet.exception.AssetNotFoundException;
import com.exchange.wallet.exception.WalletNotFoundException;
import com.exchange.wallet.repository.InMemoryWalletRepository;
import com.exchange.wallet.service.TransactionService;
import com.exchange.wallet.service.WalletService;
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
    private final TransactionService transactionService;


    @Override
    public Wallet save(Long userId, List<AssetDTO> assets) {
        Wallet existingWallet = repository.findByUserId(userId);

        // Create new
        if (existingWallet == null) {
            Wallet wallet = createWallet(userId, assets);
            repository.save(wallet);
            return wallet;
        }

        // Update
        Map<AssetType, BigDecimal> oldBalances =
                existingWallet.getAsserts().stream()
                        .collect(Collectors.toMap(
                                Asset::getAssetType,
                                Asset::getBalance
                        ));

        mergeAssets(existingWallet, assets);
        existingWallet.setUpdatedAt(LocalDateTime.now());

        repository.save(existingWallet);

        existingWallet.getAsserts().forEach(asset -> {

            BigDecimal balanceBefore = oldBalances.getOrDefault(
                    asset.getAssetType(),
                    BigDecimal.ZERO
            );

            BigDecimal balanceAfter = asset.getBalance();

            if (balanceBefore.compareTo(balanceAfter) == 0) {
                return;
            }

            TransactionType type =
                    balanceAfter.compareTo(balanceBefore) > 0
                            ? TransactionType.DEPOSIT
                            : TransactionType.WITHDRAW;

            transactionService.createTransaction(
                    existingWallet.getWalletId(),
                    existingWallet.getUserId(),
                    asset.getAssetType(),
                    balanceBefore,
                    balanceAfter,
                    type
            );
        });

        return existingWallet;
    }

    @Override
    public Wallet depositWallet(String walletId,  AssetType assetType, BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new AmountMustBePositiveException();
        }

        Wallet wallet = repository.findById(walletId)
                .orElseThrow(WalletNotFoundException::new);

        Asset asset = wallet.getAsserts().stream()
                .filter(a -> a.getAssetType().equals(assetType))
                .findFirst()
                .orElseThrow(AssetNotFoundException::new);

        asset.increase(amount);
        wallet.setUpdatedAt(LocalDateTime.now());

        repository.save(wallet);
        return wallet;
    }

    @Override
    public Wallet withdrawWallet(String walletId, AssetType type, BigDecimal amount) {
        Wallet wallet = repository.findById(walletId)
                .orElseThrow(WalletNotFoundException::new);

        Asset asset = wallet.getAsserts().stream()
                .filter(a -> a.getAssetType().equals(type))
                .findFirst()
                .orElseThrow(AssetNotFoundException::new);

        BigDecimal balanceBefore = asset.getBalance();

        asset.withdraw(amount);

        BigDecimal balanceAfter = asset.getBalance();

        wallet.setUpdatedAt(LocalDateTime.now());

        repository.save(wallet);

        transactionService.createTransaction(
                wallet.getWalletId(),
                wallet.getUserId(),
                type,
                balanceBefore,
                balanceAfter,
                TransactionType.WITHDRAW
        );

        return wallet;
    }

    @Override
    public Wallet userWalletInfo(Long onlineUser, AssetType assetType) {
        return repository
                .findByUserIdAndAssetType(onlineUser, assetType)
                .orElse(null);
    }

    @Override
    public Wallet findWalletById(String walletId) {
        return repository.findById(walletId)
                .orElseThrow(WalletNotFoundException::new);
    }

    @Override
    public BigDecimal findBalanceByUserId(Long onlineUser, AssetType assetType) {
        Wallet wallet = repository.findByUserId(onlineUser);
        if (wallet == null) {
            throw new WalletNotFoundException();
        }

        return wallet.getAsserts()
                .stream()
                .filter(asset -> asset.getAssetType() == assetType)
                .findFirst()
                .map(Asset::getBalance)
                .orElse(BigDecimal.ZERO);
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

    private Wallet createWallet(long userId, List<AssetDTO> assets) {
        List<Asset> assetEntities = assetEntities(assets);

        Wallet wallet = Wallet.builder()
                .walletId(UUID.randomUUID().toString())
                .userId(userId)
                .asserts(assetEntities)
                .status(WalletStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        wallet.getAsserts().forEach(asset -> {
            if (asset.getBalance().compareTo(BigDecimal.ZERO) > 0) {
                transactionService.createTransaction(
                        wallet.getWalletId(),
                        wallet.getUserId(),
                        asset.getAssetType(),
                        BigDecimal.ZERO,
                        asset.getBalance(),
                        TransactionType.DEPOSIT);
            }
        });

        return wallet;
    }

    private void mergeAssets(Wallet wallet, List<AssetDTO> assets) {
        List<Asset> newAssets = assetEntities(assets);
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
                wallet.getAsserts().add(newAsset);
            }
        }
    }
}
