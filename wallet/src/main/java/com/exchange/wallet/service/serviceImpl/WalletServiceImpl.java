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
        Optional<Wallet> walletOptional = repository.findByUserId(userId);

        // Create new wallet
        if (walletOptional.isEmpty()) {

            Wallet wallet = createWallet(userId, assets);

            repository.save(wallet);

            return wallet;
        }

        Wallet wallet = walletOptional.get();

        // Keep old values for transaction audit
        Map<AssetType, BigDecimal> oldBalances =
                wallet.getAssets()
                        .stream()
                        .collect(Collectors.toMap(
                                Asset::getAssetType,
                                Asset::getBalance
                        ));

        Map<AssetType, BigDecimal> oldBlockedBalances =
                wallet.getAssets()
                        .stream()
                        .collect(Collectors.toMap(
                                Asset::getAssetType,
                                Asset::getBlockedBalance
                        ));


        mergeAssets(wallet, assets);

        wallet.setUpdatedAt(LocalDateTime.now());

        wallet.getAssets().forEach(asset -> {

            BigDecimal balanceBefore =
                    oldBalances.getOrDefault(
                            asset.getAssetType(),
                            BigDecimal.ZERO
                    );

            BigDecimal balanceAfter = asset.getBalance();

            BigDecimal blockedBefore =
                    oldBlockedBalances.getOrDefault(
                            asset.getAssetType(),
                            BigDecimal.ZERO
                    );

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


        repository.save(wallet);


        return wallet;
    }

    @Override
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

    @Override
    public Wallet withdrawWallet(String walletId, AssetType type, BigDecimal amount) {
        Wallet wallet = repository.findById(walletId)
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
        repository.save(wallet);

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

    @Override
    public Wallet userWalletInfo(Long onlineUser, AssetType assetType) {
        return repository.findByUserIdAndAssetType(onlineUser, assetType)
                 .orElseThrow(WalletNotFoundException::new);
    }

    @Override
    public Wallet findWalletById(String walletId) {
        return repository.findById(walletId)
                .orElseThrow(WalletNotFoundException::new);
    }

    @Override
    public BigDecimal findBalanceByUserId(Long onlineUser, AssetType assetType) {
        Wallet wallet = findWalletByUserId(onlineUser);

        return wallet.getAssets()
                .stream()
                .filter(asset -> asset.getAssetType() == assetType)
                .findFirst()
                .map(Asset::getBalance)
                .orElse(BigDecimal.ZERO);
    }

    @Override
    public void blockWalletAmount(Long userId, AssetType assetType, BigDecimal amount) {
        Wallet wallet = findWalletByUserId(userId);

        Asset asset = getAsset(wallet, assetType);

        BigDecimal balanceBefore = asset.getBalance();
        BigDecimal blockedBefore = asset.getBlockedBalance();

        // Move amount to blocked balance
        asset.block(amount);

        BigDecimal balanceAfter = asset.getBalance();
        BigDecimal blockedAfter = asset.getBlockedBalance();

        wallet.setUpdatedAt(LocalDateTime.now());
        repository.save(wallet);

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

    @Override
    public void consumeBlockedAmount(Long userId, AssetType assetType, BigDecimal amount) {
        Wallet wallet = findWalletByUserId(userId);

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

    @Override

    public void unblockWalletAmount(Long userId, AssetType assetType, BigDecimal amount) {

        Wallet wallet = findWalletByUserId(userId);
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

    private Wallet createWallet(long userId, List<AssetDTO> assets) {
        String walletId = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();

        List<Asset> assetEntities = assetEntities(assets);

        Wallet wallet = Wallet.builder()
                .walletId(walletId)
                .userId(userId)
                .assets(assetEntities)
                .status(WalletStatus.ACTIVE)
                .createdAt(now)
                .updatedAt(now)
                .build();


        for (Asset asset : assetEntities) {

            if (asset.getBalance().compareTo(BigDecimal.ZERO) > 0) {

                transactionService.createTransaction(
                        wallet.getUserId(),
                        wallet.getWalletId(),
                        asset.getAssetType(),
                        TransactionType.DEPOSIT,

                        BigDecimal.ZERO,              // balance before
                        asset.getBalance(),            // balance after

                        BigDecimal.ZERO,              // blocked before
                        asset.getBlockedBalance()     // blocked after
                );
            }
        }

        return wallet;
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
        return repository.save(wallet);
    }

    private Wallet findWalletByUserId(Long userId) {

        return repository.findByUserId(userId)
                .orElseThrow(WalletNotFoundException::new);
    }
}
