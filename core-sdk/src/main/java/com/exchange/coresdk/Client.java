package com.exchange.coresdk;

import com.exchange.coresdk.domain.*;
import com.exchange.coresdk.domain.OrderBookDepthResponse;
import com.exchange.coresdk.domain.OrderInfoResponse;
import com.exchange.coresdk.domain.PriceLevelResponse;
import com.exchange.coresdk.domain.WalletResponse;
import com.exchange.me.sbe.*;
import com.exchange.me.sbe.MessageHeaderEncoder;


import com.exchange.wallet.sbe.AssetType;
import com.exchange.wallet.sbe.WalletRequestEncoder;
import io.aeron.Publication;
import io.aeron.cluster.client.AeronCluster;
import io.aeron.cluster.client.EgressListener;
import io.aeron.driver.MediaDriver;
import io.aeron.driver.ThreadingMode;
import io.aeron.logbuffer.Header;
import org.agrona.CloseHelper;
import org.agrona.DirectBuffer;
import org.agrona.ExpandableArrayBuffer;
import org.agrona.concurrent.Agent;
import org.agrona.concurrent.AgentRunner;
import org.agrona.concurrent.SleepingMillisIdleStrategy;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class Client implements EgressListener, AutoCloseable {

    private static final String INGRESS_CHANNEL = "aeron:udp";
    private static final String INGRESS_ENDPOINTS = "0=localhost:9002";
    private static final String EGRESS_CHANNEL = "aeron:udp?endpoint=localhost:0";
    private static final long KEEP_ALIVE_INTERVAL_NS = TimeUnit.SECONDS.toNanos(5);

    public record OrderInfo(
            long correlationId,
            long orderId,
            long timestamp,
            long userId,
            MatchStatus matchStatus,
            long filledQuantity) {
    }

    private final MediaDriver mediaDriver;
    private volatile AeronCluster aeronCluster;

    private final MessageHeaderEncoder messageHeaderEncoder = new MessageHeaderEncoder();
    private final PutOrderEncoder putOrderEncoder = new PutOrderEncoder();
    private final GetOrderInfoEncoder getOrderInfoEncoder = new GetOrderInfoEncoder();
    private final ErrorMessageDecoder errorMessageDecoder = new ErrorMessageDecoder();
    private final ExpandableArrayBuffer sendBuffer = new ExpandableArrayBuffer();
    private final OrderBookDepthEncoder orderBookDepthEncoder = new OrderBookDepthEncoder();
    private final MarketDepthDecoder marketDepthDecoder = new MarketDepthDecoder();
    private final TakeSnapShotResponseDecoder takeSnapShotResponseDecoder = new TakeSnapShotResponseDecoder();

    private final MessageHeaderDecoder messageHeaderDecoder = new MessageHeaderDecoder();
    private final OrderInfoDecoder orderInfoDecoder = new OrderInfoDecoder();

    private final CancelOrderEncoder cancelOrderEncoder = new CancelOrderEncoder();


    /**
     * =====================================================
     * Wallet
     * =====================================================
     */
    private final com.exchange.wallet.sbe.MessageHeaderEncoder walletMessageHeaderEncoder = new com.exchange.wallet.sbe.MessageHeaderEncoder();
    private final WalletRequestEncoder walletRequestEncoder = new WalletRequestEncoder();

    /**
     * =====================================================
     * Snapshot
     * =====================================================
     */

    private final TakeSnapShotDecoder takeSnapShotDecoder = new TakeSnapShotDecoder();
    private final TakeSnapShotEncoder takeSnapShotEncoder = new TakeSnapShotEncoder();

    private final ExecutorService virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();

    private final Map<Long, CompletableFuture<Response>> pendingRequests = new ConcurrentHashMap<>();
    private final AtomicLong correlationIdSequence = new AtomicLong();
    private final AgentRunner egressPollerRunner;

    public Client() {
        this.mediaDriver =
                MediaDriver.launchEmbedded(
                        new MediaDriver.Context()
                                .threadingMode(ThreadingMode.SHARED)
                                .dirDeleteOnStart(true)
                                .dirDeleteOnShutdown(true));

        this.aeronCluster = connectToCluster();

        this.egressPollerRunner =
                new AgentRunner(
                        new SleepingMillisIdleStrategy(1),
                        Throwable::printStackTrace,
                        null,
                        new Agent() {
                            private long nextKeepAliveDeadlineNs = System.nanoTime();

                            @Override
                            public int doWork() {
                                int workCount = aeronCluster.pollEgress();

                                final long nowNs = System.nanoTime();
                                if (nowNs >= nextKeepAliveDeadlineNs) {
                                    aeronCluster.sendKeepAlive();
                                    nextKeepAliveDeadlineNs = nowNs + KEEP_ALIVE_INTERVAL_NS;
                                    workCount++;
                                }

                                return workCount;
                            }

                            @Override
                            public String roleName() {
                                return "core-sdk-client-egress-poller";
                            }
                        });

        AgentRunner.startOnThread(egressPollerRunner);
    }

    private AeronCluster connectToCluster() {
        return AeronCluster.connect(
                new AeronCluster.Context()
                        .aeronDirectoryName(mediaDriver.aeronDirectoryName())
                        .egressListener(this)
                        .ingressChannel(INGRESS_CHANNEL)
                        .ingressEndpoints(INGRESS_ENDPOINTS)
                        .egressChannel(EGRESS_CHANNEL));
    }

    private synchronized void reconnect() {
        CloseHelper.quietClose(aeronCluster);
        aeronCluster = connectToCluster();
    }

    public CompletableFuture<OrderInfoResponse> putOrder(
            final long orderId,
            final long timestamp,
            final long userId,
            final TradeSide tradeSide,
            final OrderType orderType,
            final TradePair tradePair,
            final MarketType marketType,
            final long quantity,
            final long price) {

        final long correlationId = nextCorrelationId();
        System.out.println("AERON REQUEST " + "orderId=" + orderId + ", correlationId=" + correlationId);


        final ExpandableArrayBuffer sendBuffer =
                new ExpandableArrayBuffer();
        final MessageHeaderEncoder messageHeaderEncoder =
                new MessageHeaderEncoder();

        final PutOrderEncoder putOrderEncoder =
                new PutOrderEncoder();

        putOrderEncoder
                .wrapAndApplyHeader(sendBuffer, 0, messageHeaderEncoder)
                .correlationId(correlationId)
                .orderId(orderId)
                .timestamp(timestamp)
                .userId(userId)
                .tradeSide(tradeSide)
                .orderType(orderType)
                .tradePair(tradePair)
                .marketType(marketType)
                .matchStatus(MatchStatus.SUBMITED)
                .quantity(quantity)
                .price(price)
                .filled(0)
                .expireDays(PutOrderEncoder.expireDaysNullValue());

        final CompletableFuture<Response> future = new CompletableFuture<>();
        pendingRequests.put(correlationId, future);
        System.out.println("SBE CLIENT");
        sendRequest(future,
                correlationId, messageHeaderEncoder.encodedLength() + putOrderEncoder.encodedLength(), sendBuffer);
        System.out.println("SBE CLIENT after sendRequest");
        System.out.println(
                "AERON REQUEST orderId=" + orderId +
                        ", correlationId=" + correlationId
        );
        return future.thenApplyAsync(
                response -> {
                    if (response instanceof OrderInfoResponse orderInfoResponse) {
                        System.out.println("SBE CLIENT orderInfoResponse" + orderInfoResponse);
                        return orderInfoResponse;
                    } else {
                        return new OrderInfoResponse(response.getErrorCode());
                    }
                }, virtualThreadExecutor
        );
    }

    public CompletableFuture<OrderInfoResponse> cancelOrder(final long orderId, final TradePair tradePair) {
        final long correlationId = nextCorrelationId();
        final CompletableFuture<Response> future = new CompletableFuture<>();
        pendingRequests.put(correlationId, future);

        cancelOrderEncoder.wrapAndApplyHeader(
                        sendBuffer, 0, messageHeaderEncoder
                ).correlationId(correlationId)
                .orderId(orderId)
                .tradePair(tradePair);

        sendRequest(future, correlationId, messageHeaderEncoder.encodedLength() + cancelOrderEncoder.encodedLength(), null);
        return future.thenApplyAsync(
                response -> {
                    if (response instanceof OrderInfoResponse orderInfoResponse) {
                        return orderInfoResponse;
                    } else {
                        return new OrderInfoResponse(response.getErrorCode());
                    }
                }, virtualThreadExecutor
        );
    }

    public CompletableFuture<OrderInfoResponse> getOrder(final long orderId, final TradePair tradePair) {
        final long correlationId = nextCorrelationId();

        final CompletableFuture<Response> future = new CompletableFuture<>();
        pendingRequests.put(correlationId, future);

        getOrderInfoEncoder
                .wrapAndApplyHeader(sendBuffer, 0, messageHeaderEncoder)
                .correlationId(correlationId)
                .orderId(orderId)
                .tradePair(tradePair);

        sendRequest(future,
                correlationId, messageHeaderEncoder.encodedLength() + getOrderInfoEncoder.encodedLength(), null);

        return future.thenApplyAsync(
                response -> {
                    if (response instanceof OrderInfoResponse orderInfoResponse) {
                        return orderInfoResponse;
                    } else {
                        return new OrderInfoResponse(response.getErrorCode());
                    }
                }, virtualThreadExecutor
        );
    }

    public CompletableFuture<OrderBookDepthResponse> getOrderBookDepth(final TradePair pair, final int depth) {
        final long correlationId = nextCorrelationId();

        System.out.println("REQUEST SEND correlationId= " + correlationId + "pair = " + pair + "depth = " + depth);
        final CompletableFuture<Response> future = new CompletableFuture<>();
        pendingRequests.put(correlationId, future);

        orderBookDepthEncoder
                .wrapAndApplyHeader(sendBuffer, 0, messageHeaderEncoder)
                .correlationId(correlationId)
                .pair(pair)
                .depth(depth);

        sendRequest(future,
                correlationId, messageHeaderEncoder.encodedLength() + orderBookDepthEncoder.encodedLength(), null);

        return future.thenApplyAsync(
                response -> {
                    if (response instanceof OrderBookDepthResponse orderBookDepthResponse) {
                        return orderBookDepthResponse;
                    } else {
                        return new OrderBookDepthResponse(response.getErrorCode());
                    }
                }, virtualThreadExecutor
        );
    }

    /***
     *
     *  ==========================================================
     *
     *  Wallet Client
     *
     *   ==========================================================
     */

    public CompletableFuture<WalletResponse> withdrawWallet(
            String walletId, AssetType assetType, BigDecimal amount) {

        final long correlationId = nextCorrelationId();

        walletRequestEncoder
                .wrapAndApplyHeader(sendBuffer, 0, walletMessageHeaderEncoder)
                .correlationId(correlationId)
                .assetType(assetType);

        walletRequestEncoder.amount()
                .mantissa(amount.unscaledValue().longValue())
                .exponent((byte) -amount.scale());

        walletRequestEncoder.walletId(walletId);

        final CompletableFuture<Response> future = new CompletableFuture<>();
        pendingRequests.put(correlationId, future);
        System.out.println("SBE CLIENT");
        sendRequest(future,
                correlationId, messageHeaderEncoder.encodedLength() + walletRequestEncoder.encodedLength(), null);
        System.out.println("SBE CLIENT after sendRequest");
        return future.thenApplyAsync(
                response -> {
                    if (response instanceof WalletResponse walletResponse) {
                        System.out.println("SBE CLIENT Wallet Response" + walletResponse);
                        return walletResponse;
                    } else {
                        return new WalletResponse(response.getErrorCode());
                    }
                }, virtualThreadExecutor
        );
    }


    public CompletableFuture<TakeSnapshotResponse> takeSnapShot() {
        final CompletableFuture<Response> future = new CompletableFuture<>();

        final long correlationId = nextCorrelationId();

        pendingRequests.put(correlationId, future);
        System.out.println("takesnapshot-----------------------");
        takeSnapShotEncoder
                .wrapAndApplyHeader(sendBuffer, 0, messageHeaderEncoder)
                .correlationId(correlationId);
        sendRequest(future,
                correlationId, messageHeaderEncoder.encodedLength() + takeSnapShotEncoder.encodedLength(), null);

        System.out.println("takesnapshot2222222222222-----------------------");
        return future.thenApplyAsync(
                response -> {
                    if (response instanceof TakeSnapshotResponse takeSnapshotResponse) {
                        System.out.println("takesnapshot-----------------------");
                        return takeSnapshotResponse;
                    } else {
                        return new TakeSnapshotResponse(response.getErrorCode());
                    }
                }, virtualThreadExecutor
        );

    }


    private long nextCorrelationId() {
        return correlationIdSequence.incrementAndGet();
    }

    private void sendRequest(CompletableFuture<Response> future, final long correlationId, final int length, final DirectBuffer sendBuffer) {
        System.out.println("AERON SEND START correlationId=" + correlationId);

        long result = offer(sendBuffer, length);

        System.out.println(
                "AERON SEND END correlationId=" + correlationId +
                        ", result=" + result
        );
        System.out.println("Sending aeron request correlationId=" + correlationId);


        if (result == Publication.CLOSED || result == Publication.NOT_CONNECTED) {
            // The cluster connection died (e.g. the cluster node was restarted) - reconnect once
            // and retry before giving up.
            reconnect();
            result = offer(sendBuffer, length);
        }


        if (result < 0) {
            System.out.println("Error on sending request: " + result);
            pendingRequests.remove(correlationId);
            future.completeExceptionally(
                    new IllegalStateException("offer failed: " + describeOfferResult(result)));
        }
        System.out.println(
                "AERON OFFER " +
                        ", correlationId=" + correlationId +
                        ", result=" + result
        );
    }

    private long offer(
            final DirectBuffer sendBuffer,
            final int length) {

        long result;
        int attempts = 0;
        do {
            result = aeronCluster.offer(
                    sendBuffer,
                    0,
                    length
            );
            attempts++;
            if (result == Publication.BACK_PRESSURED ||
                    result == Publication.ADMIN_ACTION) {

                if (attempts % 1000 == 0) {
                    System.out.println(
                            "AERON OFFER RETRY " +
                                    "result=" + result +
                                    ", attempts=" + attempts
                    );
                }
            }

        } while (
                result == Publication.ADMIN_ACTION ||
                        result == Publication.BACK_PRESSURED
        );

        return result;
    }

    @Override
    public void onMessage(
            final long clusterSessionId,
            final long timestamp,
            final DirectBuffer buffer,
            final int offset,
            final int length,
            final Header header) {

        messageHeaderDecoder.wrap(buffer, offset);

        final int headerLength = messageHeaderDecoder.encodedLength();
        final int actingBlockLength = messageHeaderDecoder.blockLength();
        final int actingVersion = messageHeaderDecoder.version();

        switch (messageHeaderDecoder.templateId()) {
            case OrderInfoDecoder.TEMPLATE_ID: {

                orderInfoDecoder.wrap(
                        buffer,
                        offset + headerLength,
                        actingBlockLength,
                        actingVersion
                );

                long correlationId = orderInfoDecoder.correlationId();
                long orderId = orderInfoDecoder.orderId();

                System.out.println( "AERON RESPONSE " +
                                "correlationId=" + correlationId +
                                ", orderId=" + orderId +
                                ", pending=" + pendingRequests.containsKey(correlationId)
                );

                final CompletableFuture<Response> future =
                        pendingRequests.remove(correlationId);

                System.out.println(
                        "AERON COMPLETE " +
                                "correlationId=" + correlationId +
                                ", futureFound=" + (future != null)
                );

                if (future != null) {
                    future.complete(
                            new OrderInfoResponse(
                                    orderId,
                                    orderInfoDecoder.timestamp(),
                                    orderInfoDecoder.userId(),
                                    orderInfoDecoder.matchStatus(),
                                    orderInfoDecoder.filledQuantity()
                            )
                    );
                }

                break;
            }

            case MarketDepthDecoder.TEMPLATE_ID: {
                marketDepthDecoder.wrap(buffer, offset + headerLength, actingBlockLength, actingVersion);

                final CompletableFuture<Response> future =
                        pendingRequests.remove(marketDepthDecoder.correlationId());
                if (future != null) {
                    MarketDepthDecoder.BidsDecoder bids = marketDepthDecoder.bids();
                    Iterator<MarketDepthDecoder.BidsDecoder> bidsIterator = bids.iterator();

                    MarketDepthDecoder.AsksDecoder asks = marketDepthDecoder.asks();
                    Iterator<MarketDepthDecoder.AsksDecoder> asksIterator = asks.iterator();

                    List<PriceLevelResponse> bidsList = new ArrayList<>();
                    List<PriceLevelResponse> asksList = new ArrayList<>();


                    while (bidsIterator.hasNext()) {
                        MarketDepthDecoder.BidsDecoder bidsDecoder = bidsIterator.next();
                        bidsList.add(new PriceLevelResponse(bidsDecoder.price(), bidsDecoder.volume(), bidsDecoder.orderCount()));
                    }
                    while (asksIterator.hasNext()) {
                        MarketDepthDecoder.AsksDecoder askDecoder = asksIterator.next();
                        asksList.add(new PriceLevelResponse(askDecoder.price(), askDecoder.volume(), askDecoder.orderCount()));
                    }
                    future.complete(
                            new OrderBookDepthResponse(bidsList, asksList)
                    );

                }

                break;
            }

            case ErrorMessageDecoder.TEMPLATE_ID: {
                errorMessageDecoder.wrap(buffer, offset + headerLength, actingBlockLength, actingVersion);
                final CompletableFuture<Response> future =
                        pendingRequests.remove(errorMessageDecoder.correlationId());
                if (future != null) {
                    future.complete(
                            new Response(errorMessageDecoder.code()));
                }
                break;
            }

            case TakeSnapShotResponseDecoder.TEMPLATE_ID: {
                takeSnapShotResponseDecoder.wrap(buffer, offset + headerLength, actingBlockLength, actingVersion);
                final CompletableFuture<Response> future =
                        pendingRequests.remove(takeSnapShotResponseDecoder.correlationId());
                if (future != null) {
                    future.complete(
                            new Response(0));
                }
                break;
            }
        }
    }

    private static String describeOfferResult(final long result) {
        if (result == Publication.NOT_CONNECTED) {
            return "NOT_CONNECTED (" + result + ")";
        }
        if (result == Publication.CLOSED) {
            return "CLOSED (" + result + ")";
        }
        if (result == Publication.MAX_POSITION_EXCEEDED) {
            return "MAX_POSITION_EXCEEDED (" + result + ")";
        }
        return String.valueOf(result);
    }

    @Override
    public void close() {
        CloseHelper.quietClose(egressPollerRunner);
        CloseHelper.quietClose(aeronCluster);
        CloseHelper.quietClose(mediaDriver);
    }
}
