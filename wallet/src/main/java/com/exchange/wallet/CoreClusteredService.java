package com.exchange.wallet;


import com.exchange.metrics.ClusterMetrics;
import com.exchange.wallet.domain.Asset;
import com.exchange.wallet.domain.AssetType;
import com.exchange.wallet.domain.ErrorCode;
import com.exchange.wallet.domain.Wallet;
import com.exchange.wallet.domain.WalletStatus;
import com.exchange.wallet.sbe.MessageHeaderDecoder;
import com.exchange.wallet.sbe.MessageHeaderEncoder;
import com.exchange.wallet.sbe.CreateWalletRequestDecoder;
import com.exchange.wallet.sbe.WalletEncoder;
import com.exchange.wallet.sbe.WalletErrorMessageEncoder;
import com.exchange.wallet.sbe.SnapshotWalletEncoder;
import com.exchange.wallet.sbe.SnapshotWalletDecoder;
import io.aeron.ExclusivePublication;
import io.aeron.Image;
import io.aeron.Publication;
import io.aeron.cluster.codecs.CloseReason;
import io.aeron.cluster.service.ClientSession;
import io.aeron.cluster.service.Cluster;
import io.aeron.cluster.service.ClusteredService;
import io.aeron.logbuffer.FragmentHandler;
import io.aeron.logbuffer.Header;
import io.micrometer.core.instrument.Counter;
import lombok.extern.slf4j.Slf4j;
import io.micrometer.core.instrument.Timer;

import org.agrona.DirectBuffer;
import org.agrona.ExpandableDirectByteBuffer;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;


@Slf4j
public class CoreClusteredService implements ClusteredService {
    private static final long METRICS_LOG_INTERVAL_NS = TimeUnit.SECONDS.toNanos(10);

    private final MessageHeaderDecoder messageHeaderDecoder;
    private final MessageHeaderEncoder messageHeaderEncoder;
    private final ExpandableDirectByteBuffer respondBuffer;
    private final CreateWalletRequestDecoder createWalletRequestDecoder;
    private final WalletEncoder walletEncoder;
    private final WalletErrorMessageEncoder walletErrorMessageEncoder;
    private final SnapshotWalletEncoder snapshotWalletEncoder;
    private final SnapshotWalletDecoder snapshotWalletDecoder;

    private final ExpandableDirectByteBuffer snapshotBuffer;

    private final Counter receivedMessages;
    private final Counter failedResponses;
    private final Counter successfulResponses;
    private final Counter backPressuredResponses;
    private final Counter adminActionResponses;
    public final Timer requestProcessingTimer;
    public final Timer responseOfferTimer;




    private final Map<String, Wallet> wallets;
    private Cluster cluster;
    private long nextMetricsLogNs;



    public CoreClusteredService(ClusterMetrics metrics) {
        log.info("Initializing CoreClusteredService");

        messageHeaderDecoder = new MessageHeaderDecoder();
        messageHeaderEncoder = new MessageHeaderEncoder();
        this.respondBuffer = new ExpandableDirectByteBuffer(1024);
        snapshotBuffer = new ExpandableDirectByteBuffer(1024 * 1024);
        createWalletRequestDecoder = new CreateWalletRequestDecoder();
        walletEncoder = new WalletEncoder();
        walletErrorMessageEncoder = new WalletErrorMessageEncoder();
        snapshotWalletEncoder = new SnapshotWalletEncoder();
        snapshotWalletDecoder = new SnapshotWalletDecoder();
        wallets = new LinkedHashMap<>();
        this.receivedMessages = metrics.receivedMessages;
        this.failedResponses = metrics.failedResponses;
        this.successfulResponses = metrics.successfulResponses;
        this.backPressuredResponses = metrics.backPressuredResponses;
        this.adminActionResponses = metrics.adminActionResponses;
        this.requestProcessingTimer = metrics.requestProcessingTimer;
        this.responseOfferTimer = metrics.responseOfferTimer;

    }

    @Override
    public void onStart(final Cluster cluster, final Image snapshotImage) {
        this.cluster = cluster;

        log.info("Clustered service started: memberId={}, hasSnapshot={}", cluster.memberId(), snapshotImage != null);

        if (snapshotImage != null) {
            loadSnapshot(snapshotImage);
        } else {
            log.info("No snapshot available, starting with empty engine state");
        }
    }


    @Override
    public void onSessionOpen(final ClientSession session, final long timestamp) {

        log.info("Client session opened: sessionId={}, timestamp={}", session.id(), timestamp);
    }

    @Override
    public void onSessionClose(final ClientSession session, final long timestamp, final CloseReason closeReason) {
        log.info("Client session closed: sessionId={}, timestamp={}, reason={}", session.id(), timestamp, closeReason);
    }

    @Override
    public void onSessionMessage(
            final ClientSession session,
            final long timestamp,
            final DirectBuffer buffer,
            final int offset,
            final int length,
            final Header header) {

        receivedMessages.increment();

        if (session == null) {
            log.warn("Received message with null session: offset={}, length={}", offset, length);
            return;
        }

        messageHeaderDecoder.wrap(buffer, offset);
        int templateId = messageHeaderDecoder.templateId();

        final int headerLength = messageHeaderDecoder.encodedLength();
        final int actingLength = messageHeaderDecoder.blockLength();
        final int actingVersion = messageHeaderDecoder.version();

        if (templateId != CreateWalletRequestDecoder.TEMPLATE_ID) {
            log.warn("Unsupported wallet request templateId={}", templateId);
            failedResponses.increment();
            return;
        }

        final Timer.Sample requestSample = Timer.start();
        try {
            createWalletRequestDecoder.wrap(
                    buffer, offset + headerLength, actingLength, actingVersion);
            final Wallet wallet = createWallet(createWalletRequestDecoder, timestamp);
            encodeWalletResponse(session, createWalletRequestDecoder.correlationId(), wallet);
            successfulResponses.increment();
            requestSample.stop(requestProcessingTimer);
            logMetricsIfDue();
        } catch (RuntimeException exception) {
            failedResponses.increment();
            log.warn("Rejected wallet create request: {}", exception.getMessage());
            sendError(session, createWalletRequestDecoder.correlationId(), ErrorCode.INVALID_WALLET_REQUEST);
        }
    }

    @Override
    public void onTimerEvent(final long correlationId, final long timestamp) {
        log.debug("Cluster timer fired: correlationId={}, timestamp={}", correlationId, timestamp);
    }

    @Override
    public void onTakeSnapshot(final ExclusivePublication snapshotPublication) {
        for (Wallet wallet : wallets.values()) {
            snapshotWalletEncoder.wrapAndApplyHeader(snapshotBuffer, 0, messageHeaderEncoder)
                    .correlationId(0)
                    .userId(wallet.getUserId())
                    .status(com.exchange.wallet.sbe.WalletStatus.valueOf(wallet.getStatus().name()))
                    .timestamp(wallet.getUpdatedAt().atZone(ZoneId.systemDefault())
                            .toInstant().toEpochMilli());
            final UUID walletId = UUID.fromString(wallet.getWalletId());
            snapshotWalletEncoder.walletId()
                    .mostSignificantBits(walletId.getMostSignificantBits())
                    .leastSignificantBits(walletId.getLeastSignificantBits());
            final SnapshotWalletEncoder.AssetsEncoder assetsEncoder =
                    snapshotWalletEncoder.assetsCount(wallet.getAssets().size());
            for (Asset asset : wallet.getAssets()) {
                final SnapshotWalletEncoder.AssetsEncoder assetEncoder = assetsEncoder.next()
                        .assetType(com.exchange.wallet.sbe.AssetType.valueOf(asset.getAssetType().name()));
                encodeDecimal(assetEncoder.balance(), asset.getBalance());
                encodeDecimal(assetEncoder.blockedBalance(), asset.getBlockedBalance());
            }
            offerSnapshot(snapshotPublication,
                    messageHeaderEncoder.encodedLength() + snapshotWalletEncoder.encodedLength());
        }
        log.info("Wallet snapshot taken: walletCount={}", wallets.size());
    }


    @Override
    public void onRoleChange(final Cluster.Role newRole) {

        log.info("Cluster role changed: newRole={}", newRole);
    }

    @Override
    public void onTerminate(final Cluster cluster) {
        log.info("Cluster node terminating: clusterId={}", cluster.memberId());
    }

    private void loadSnapshot(final Image snapshotImage) {
        log.info("Loading wallet snapshot");
        final FragmentHandler handler = (buffer, offset, length, header) -> {
            messageHeaderDecoder.wrap(buffer, offset);
            if (messageHeaderDecoder.templateId() != SnapshotWalletDecoder.TEMPLATE_ID) {
                log.warn("Ignoring snapshot templateId={}", messageHeaderDecoder.templateId());
                return;
            }
            snapshotWalletDecoder.wrap(buffer, offset + messageHeaderDecoder.encodedLength(),
                    messageHeaderDecoder.blockLength(), messageHeaderDecoder.version());
            final UUID walletId = new UUID(
                    snapshotWalletDecoder.walletId().mostSignificantBits(),
                    snapshotWalletDecoder.walletId().leastSignificantBits());
            final List<Asset> assets = new ArrayList<>();
            final SnapshotWalletDecoder.AssetsDecoder assetsDecoder = snapshotWalletDecoder.assets();
            while (assetsDecoder.hasNext()) {
                final SnapshotWalletDecoder.AssetsDecoder assetDecoder = assetsDecoder.next();
                assets.add(Asset.builder()
                        .assetType(AssetType.valueOf(assetDecoder.assetType().name()))
                        .balance(decodeDecimal(
                                assetDecoder.balance().mantissa(), assetDecoder.balance().exponent()))
                        .blockedBalance(decodeDecimal(
                                assetDecoder.blockedBalance().mantissa(),
                                assetDecoder.blockedBalance().exponent()))
                        .build());
            }
            final LocalDateTime updatedAt = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(snapshotWalletDecoder.timestamp()), ZoneId.systemDefault());
            final Wallet wallet = Wallet.builder()
                    .walletId(walletId.toString())
                    .userId(snapshotWalletDecoder.userId())
                    .status(WalletStatus.valueOf(snapshotWalletDecoder.status().name()))
                    .assets(assets)
                    .createdAt(updatedAt)
                    .updatedAt(updatedAt)
                    .build();
            wallets.put(wallet.getWalletId(), wallet);
        };

        while (!snapshotImage.isEndOfStream()) {
            final int fragments = snapshotImage.poll(handler, 100);
            cluster.idleStrategy().idle(fragments);
        }
        log.info("Wallet snapshot restored: walletCount={}", wallets.size());
    }

    private Wallet createWallet(final CreateWalletRequestDecoder request, final long timestamp) {
        final List<Asset> assets = new ArrayList<>();
        final Set<AssetType> requestedTypes = new HashSet<>();
        final CreateWalletRequestDecoder.AssetsDecoder assetsDecoder = request.assets();
        while (assetsDecoder.hasNext()) {
            final CreateWalletRequestDecoder.AssetsDecoder assetDecoder = assetsDecoder.next();
            final AssetType assetType = AssetType.valueOf(assetDecoder.assetType().name());
            if (!requestedTypes.add(assetType) || hasAsset(request.userId(), assetType)) {
                throw new IllegalArgumentException(
                        "Wallet already contains asset type: " + assetType);
            }
            final BigDecimal balance =
                    decodeDecimal(assetDecoder.balance().mantissa(), assetDecoder.balance().exponent());
            final BigDecimal blockedBalance =
                    decodeDecimal(assetDecoder.blockedBalance().mantissa(),
                            assetDecoder.blockedBalance().exponent());
            if (balance.signum() < 0 || blockedBalance.signum() < 0 ||
                    blockedBalance.compareTo(balance) > 0) {
                throw new IllegalArgumentException("Invalid initial balance for asset: " + assetType);
            }
            assets.add(Asset.builder()
                    .assetType(assetType)
                    .balance(balance)
                    .blockedBalance(blockedBalance)
                    .build());
        }
        if (assets.isEmpty()) {
            throw new IllegalArgumentException("A wallet must contain at least one asset");
        }

        final LocalDateTime now = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
        final Wallet wallet = Wallet.builder()
                .walletId(UUID.randomUUID().toString())
                .userId(request.userId())
                .assets(assets)
                .status(WalletStatus.ACTIVE)
                .createdAt(now)
                .updatedAt(now)
                .build();
        wallets.put(wallet.getWalletId(), wallet);
        return wallet;
    }

    private boolean hasAsset(final long userId, final AssetType assetType) {
        return wallets.values().stream()
                .filter(wallet -> wallet.getUserId() == userId)
                .flatMap(wallet -> wallet.getAssets().stream())
                .anyMatch(asset -> asset.getAssetType() == assetType);
    }

    private void encodeWalletResponse(
            final ClientSession session, final long correlationId, final Wallet wallet) {
        walletEncoder.wrapAndApplyHeader(respondBuffer, 0, messageHeaderEncoder)
                .correlationId(correlationId)
                .userId(wallet.getUserId())
                .status(com.exchange.wallet.sbe.WalletStatus.valueOf(wallet.getStatus().name()))
                .createdAt(wallet.getCreatedAt().atZone(ZoneId.systemDefault())
                        .toInstant().toEpochMilli())
                .updatedAt(wallet.getUpdatedAt().atZone(ZoneId.systemDefault())
                        .toInstant().toEpochMilli());
        final UUID walletId = UUID.fromString(wallet.getWalletId());
        walletEncoder.walletId()
                .mostSignificantBits(walletId.getMostSignificantBits())
                .leastSignificantBits(walletId.getLeastSignificantBits());
        final WalletEncoder.AssetsEncoder assetsEncoder = walletEncoder.assetsCount(wallet.getAssets().size());
        for (Asset asset : wallet.getAssets()) {
            final WalletEncoder.AssetsEncoder assetEncoder = assetsEncoder.next()
                    .assetType(com.exchange.wallet.sbe.AssetType.valueOf(asset.getAssetType().name()));
            encodeDecimal(assetEncoder.balance(), asset.getBalance());
            encodeDecimal(assetEncoder.blockedBalance(), asset.getBlockedBalance());
        }
        offerResponse(session, walletEncoder.encodedLength() + messageHeaderEncoder.encodedLength());
    }

    private void sendError(
            final ClientSession session, final long correlationId, final int code) {
        walletErrorMessageEncoder.wrapAndApplyHeader(respondBuffer, 0, messageHeaderEncoder)
                .correlationId(correlationId)
                .code(code);
        offerResponse(session,
                walletErrorMessageEncoder.encodedLength() + messageHeaderEncoder.encodedLength());
    }

    private void offerResponse(final ClientSession session, final int length) {
        long result;
        final Timer.Sample sample = Timer.start();
        do {
            result = session.offer(respondBuffer, 0, length);
            if (result == Publication.ADMIN_ACTION || result == Publication.BACK_PRESSURED) {
                if (result == Publication.ADMIN_ACTION) {
                    adminActionResponses.increment();
                } else {
                    backPressuredResponses.increment();
                }
                cluster.idleStrategy().idle();
            }
        } while (result == Publication.ADMIN_ACTION || result == Publication.BACK_PRESSURED);
        sample.stop(responseOfferTimer);
        if (result < 0) {
            throw new IllegalStateException("Wallet response offer failed: " + result);
        }
    }

    private static void encodeDecimal(
            final com.exchange.wallet.sbe.DecimalValueEncoder encoder, final BigDecimal value) {
        final int exponent = Math.negateExact(value.scale());
        if (exponent < Byte.MIN_VALUE || exponent > Byte.MAX_VALUE) {
            throw new IllegalArgumentException("Decimal scale is outside SBE exponent range: " + value.scale());
        }
        encoder.mantissa(value.unscaledValue().longValueExact()).exponent((byte) exponent);
    }

    private static BigDecimal decodeDecimal(final long mantissa, final byte exponent) {
        return BigDecimal.valueOf(mantissa).scaleByPowerOfTen(exponent);
    }

    private void offerSnapshot(final ExclusivePublication snapshotPublication, int length) {
        while (true) {
            long result = snapshotPublication.offer(snapshotBuffer, 0, length);

            if (result > 0) {
                break;
            }

            if (result == Publication.CLOSED || result == Publication.MAX_POSITION_EXCEEDED) {
                throw new IllegalStateException("Snapshot offer failed: " + result);
            }

            log.debug("========== TAKING SNAPSHOT ==========");
            Thread.yield();
        }
    }

    private void logMetricsIfDue() {
        final long nowNs = System.nanoTime();
        if (nowNs < nextMetricsLogNs) {
            return;
        }
        nextMetricsLogNs = nowNs + METRICS_LOG_INTERVAL_NS;

        log.info(
                "AERON METRICS received={}, responsesSuccess={}, responsesFailed={}, " +
                        "responsesBackPressured={}, responsesAdminAction={}, requestCount={}, " +
                        "requestMeanMs={}, requestMaxMs={}, responseOfferCount={}, " +
                        "responseOfferMeanMs={}, responseOfferMaxMs={}",
                receivedMessages.count(),
                successfulResponses.count(),
                failedResponses.count(),
                backPressuredResponses.count(),
                adminActionResponses.count(),
                requestProcessingTimer.count(),
                requestProcessingTimer.mean(TimeUnit.MILLISECONDS),
                requestProcessingTimer.max(TimeUnit.MILLISECONDS),
                responseOfferTimer.count(),
                responseOfferTimer.mean(TimeUnit.MILLISECONDS),
                responseOfferTimer.max(TimeUnit.MILLISECONDS)
        );
    }

}