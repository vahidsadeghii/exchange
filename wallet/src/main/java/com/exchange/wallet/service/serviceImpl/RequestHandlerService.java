package com.exchange.wallet.service.serviceImpl;

import com.exchange.wallet.domain.AssetType;
import com.exchange.wallet.domain.ErrorCode;
import com.exchange.wallet.domain.Wallet;
import com.exchange.wallet.controller.wallet.AssetDTO;
import com.exchange.wallet.sbe.*;

import com.exchange.wallet.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.agrona.DirectBuffer;
import org.agrona.ExpandableDirectByteBuffer;


import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;


@RequiredArgsConstructor
public class RequestHandlerService {

    private final WalletService walletService;
    private final TransactionService transactionService;

    private final TransactionInfoDecoder transactionInfoDecoder;
    private final WalletRequestDecoder walletRequestDecoder = new WalletRequestDecoder();
    private final WalletResponseEncoder walletResponseEncoder = new WalletResponseEncoder();
    private final WalletEncoder walletEncoder = new WalletEncoder();
    private final CreateWalletRequestDecoder createWalletRequestDecoder = new CreateWalletRequestDecoder();
    private final MessageHeaderEncoder messageHeaderEncoder = new MessageHeaderEncoder();
    private final WalletErrorMessageEncoder walletErrorMessageEncoder = new WalletErrorMessageEncoder();


    public int handleCreateWalletRequest(
            long sessionId,
            long timestamp,
            DirectBuffer buffer,
            int offset,
            int headerLength,
            int actingLength,
            int actingVersion,
            ExpandableDirectByteBuffer respondBuffer) {

        createWalletRequestDecoder.wrap(buffer, offset + headerLength, actingLength, actingVersion);

        final List<AssetDTO> assets = new ArrayList<>();
        final CreateWalletRequestDecoder.AssetsDecoder assetsDecoder = createWalletRequestDecoder.assets();

        while (assetsDecoder.hasNext()) {
            final CreateWalletRequestDecoder.AssetsDecoder assetDecoder = assetsDecoder.next();
            final var balance = assetDecoder.balance();
            final var blockedBalance = assetDecoder.blockedBalance();
            assets.add(new AssetDTO(
                    decodeDecimal(balance.mantissa(), balance.exponent()),
                    decodeDecimal(blockedBalance.mantissa(), blockedBalance.exponent()),
                    AssetType.valueOf(assetDecoder.assetType().name())));
        }

        final Wallet wallet = walletService.save(createWalletRequestDecoder.userId(), assets);

        if (wallet != null) {
            final var walletUuid = java.util.UUID.fromString(wallet.getWalletId());
            walletEncoder.wrapAndApplyHeader(respondBuffer, 0, messageHeaderEncoder)
                    .correlationId(createWalletRequestDecoder.correlationId())
                    .userId(wallet.getUserId())
                    .status(com.exchange.wallet.sbe.WalletStatus.valueOf(wallet.getStatus().name()))
                    .createdAt(wallet.getCreatedAt().atZone(ZoneId.systemDefault())
                            .toInstant().toEpochMilli())
                    .updatedAt(wallet.getUpdatedAt().atZone(ZoneId.systemDefault())
                            .toInstant().toEpochMilli());
            walletEncoder.walletId()
                    .mostSignificantBits(walletUuid.getMostSignificantBits())
                    .leastSignificantBits(walletUuid.getLeastSignificantBits());

            final WalletEncoder.AssetsEncoder responseAssets =
                    walletEncoder.assetsCount(wallet.getAssets().size());
            for (var asset : wallet.getAssets()) {
                final var responseAsset = responseAssets.next()
                        .assetType(com.exchange.wallet.sbe.AssetType.valueOf(asset.getAssetType().name()));
                encodeDecimal(responseAsset.balance(), asset.getBalance());
                encodeDecimal(responseAsset.blockedBalance(), asset.getBlockedBalance());
            }
            return walletEncoder.encodedLength() + messageHeaderEncoder.encodedLength();
        } else {
            return returnErrorMessage(
                    respondBuffer, createWalletRequestDecoder.correlationId(), ErrorCode.ORDER_NOT_FOUND);
        }

    }


    public int handleWithdrawWalletRequest(
            long sessionId,
            long timestamp,
            DirectBuffer buffer,
            int offset,
            int headerLength,
            int actingLength,
            int actingVersion,
            ExpandableDirectByteBuffer respondBuffer) {

        walletRequestDecoder.wrap(buffer, offset + headerLength, actingLength, actingVersion);

        var amountDecoder = walletRequestDecoder.amount();

        Wallet wallet = walletService.withdrawWallet(walletRequestDecoder.walletId(),
                AssetType.valueOf(walletRequestDecoder.assetType().name()),
                BigDecimal.valueOf(amountDecoder.mantissa()).scaleByPowerOfTen(amountDecoder.exponent()));

        if (wallet != null) {

            return walletResponseEncoder.encodedLength() + messageHeaderEncoder.encodedLength();
        } else {
            return returnErrorMessage(respondBuffer, walletRequestDecoder.correlationId(), ErrorCode.ORDER_NOT_FOUND);
        }

    }



    public int handleTransactionInfoRequest(
            long sessionId,
            long timestamp,
            DirectBuffer buffer,
            int offset,
            int headerLength,
            int actingLength,
            int actingVersion,
            ExpandableDirectByteBuffer respondBuffer) {

        transactionInfoDecoder.wrap(buffer, offset + headerLength, actingLength, actingVersion);

        return 1;

    }

    private int returnErrorMessage(ExpandableDirectByteBuffer respondBuffer, long correlationId, int errorCode) {
        walletErrorMessageEncoder.wrapAndApplyHeader(respondBuffer, 0, messageHeaderEncoder);
        walletErrorMessageEncoder.correlationId(correlationId);
        walletErrorMessageEncoder.code(errorCode);

        return walletErrorMessageEncoder.encodedLength() + messageHeaderEncoder.encodedLength();
    }

    private static BigDecimal decodeDecimal(long mantissa, byte exponent) {
        return BigDecimal.valueOf(mantissa).scaleByPowerOfTen(exponent);
    }

    private static void encodeDecimal(DecimalValueEncoder encoder, BigDecimal value) {
        int exponent = Math.negateExact(value.scale());
        if (exponent < Byte.MIN_VALUE || exponent > Byte.MAX_VALUE) {
            throw new IllegalArgumentException("Decimal scale is outside SBE exponent range: " + value.scale());
        }
        encoder.mantissa(value.unscaledValue().longValueExact())
                .exponent((byte) exponent);
    }
}
