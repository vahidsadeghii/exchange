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

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Client implements EgressListener, AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(Client.class);

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
    //private final TakeSnapShotEncoder takeSnapShotEncoder = new TakeSnapShotEncoder();

    private final ExecutorService virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();

    private final ConcurrentHashMap<Long, CompletableFuture<Response>> pendingRequests = new ConcurrentHashMap<>();
    private final Queue<IngressRequest> ingressRequests = new ConcurrentLinkedQueue<>();
    private final AtomicLong correlationIdSequence = new AtomicLong();
    private final AgentRunner egressPollerRunner;

    private record IngressRequest(
            CompletableFuture<Response> future,
            long correlationId,
            int length,
            DirectBuffer buffer) {
    }

    public Client() {
        this.mediaDriver =
                MediaDriver.launchEmbedded(
                        new MediaDriver.Context()
                                .threadingMode(ThreadingMode.DEDICATED)
                                .dirDeleteOnStart(true)
                                .dirDeleteOnShutdown(true));

        this.aeronCluster = connectToCluster();

        this.egressPollerRunner =
                new AgentRunner(
                        new org.agrona.concurrent.BusySpinIdleStrategy(),
                        Throwable::printStackTrace,
                        null,
                        new Agent() {
                            private long nextKeepAliveDeadlineNs = System.nanoTime();

                            @Override
                            public int doWork() {
                                int workCount = 0;
                                try {
                                    workCount += sendQueuedRequests();
                                    workCount += aeronCluster.pollEgress();
                                    final long nowNs = System.nanoTime();
                                    if (nowNs >= nextKeepAliveDeadlineNs) {
                                        aeronCluster.sendKeepAlive();
                                        nextKeepAliveDeadlineNs = nowNs + KEEP_ALIVE_INTERVAL_NS;
                                        workCount++;
                                    }
                                } catch (Exception e) {
                                    log.error("AERON CLIENT WORK FAILED", e);
                                    return 0;
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

    private void reconnect() {
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
        final ExpandableArrayBuffer sendBuffer = new ExpandableArrayBuffer();
        final MessageHeaderEncoder messageHeaderEncoder = new MessageHeaderEncoder();
        final PutOrderEncoder putOrderEncoder = new PutOrderEncoder();

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

        log.info("PUT ORDER QUEUED orderId={}, correlationId={}, pending={}",
                orderId, correlationId, pendingRequests.size());

        sendRequest(future,
                correlationId, messageHeaderEncoder.encodedLength() + putOrderEncoder.encodedLength(), sendBuffer);
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

        final CancelOrderEncoder cancelOrderEncoder = new CancelOrderEncoder();
        final ExpandableArrayBuffer sendBuffer = new ExpandableArrayBuffer();
        final MessageHeaderEncoder messageHeaderEncoder = new MessageHeaderEncoder();

        cancelOrderEncoder.wrapAndApplyHeader(
                        sendBuffer, 0, messageHeaderEncoder
                ).correlationId(correlationId)
                .orderId(orderId)
                .tradePair(tradePair);

        sendRequest(future, correlationId, messageHeaderEncoder.encodedLength() + cancelOrderEncoder.encodedLength(), sendBuffer);
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
        final ExpandableArrayBuffer sendBuffer = new ExpandableArrayBuffer();
        final MessageHeaderEncoder messageHeaderEncoder = new MessageHeaderEncoder();
        final GetOrderInfoEncoder getOrderInfoEncoder = new GetOrderInfoEncoder();


        final CompletableFuture<Response> future = new CompletableFuture<>();
        pendingRequests.put(correlationId, future);

        getOrderInfoEncoder
                .wrapAndApplyHeader(sendBuffer, 0, messageHeaderEncoder)
                .correlationId(correlationId)
                .orderId(orderId)
                .tradePair(tradePair);

        sendRequest(future, correlationId, messageHeaderEncoder.encodedLength() + getOrderInfoEncoder.encodedLength(), sendBuffer);

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

        final ExpandableArrayBuffer sendBuffer = new ExpandableArrayBuffer();
        final MessageHeaderEncoder messageHeaderEncoder = new MessageHeaderEncoder();
        final long correlationId = nextCorrelationId();

        System.out.println("REQUEST SEND correlationId= " + correlationId + "pair = " + pair + "depth = " + depth);
        final CompletableFuture<Response> future = new CompletableFuture<>();
        pendingRequests.put(correlationId, future);
        final OrderBookDepthEncoder orderBookDepthEncoder = new OrderBookDepthEncoder();
        orderBookDepthEncoder
                .wrapAndApplyHeader(sendBuffer, 0, messageHeaderEncoder)
                .correlationId(correlationId)
                .pair(pair)
                .depth(depth);

        sendRequest(future,
                correlationId, messageHeaderEncoder.encodedLength() + orderBookDepthEncoder.encodedLength(), sendBuffer);

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

        final ExpandableArrayBuffer sendBuffer = new ExpandableArrayBuffer();
        final MessageHeaderEncoder messageHeaderEncoder = new MessageHeaderEncoder();
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
                correlationId, messageHeaderEncoder.encodedLength() + walletRequestEncoder.encodedLength(), sendBuffer);
        System.out.println("SBE CLIENT after sendRequest");
        return future.thenApplyAsync(
                response -> {
                    if (response instanceof WalletResponse walletResponse) {
                        log.info("SBE CLIENT Wallet Response" + walletResponse);
                        return walletResponse;
                    } else {
                        return new WalletResponse(response.getErrorCode());
                    }
                }, virtualThreadExecutor
        );
    }


    public CompletableFuture<TakeSnapshotResponse> takeSnapShot() {
        final CompletableFuture<Response> future = new CompletableFuture<>();
        final TakeSnapShotEncoder takeSnapShotEncoder = new TakeSnapShotEncoder();
        final long correlationId = nextCorrelationId();

        final ExpandableArrayBuffer sendBuffer = new ExpandableArrayBuffer();
        final MessageHeaderEncoder messageHeaderEncoder = new MessageHeaderEncoder();
        pendingRequests.put(correlationId, future);
       log.info("takesnapshot-----------------------");
        takeSnapShotEncoder
                .wrapAndApplyHeader(sendBuffer, 0, messageHeaderEncoder)
                .correlationId(correlationId);
        sendRequest(future,
                correlationId, messageHeaderEncoder.encodedLength() + takeSnapShotEncoder.encodedLength(), sendBuffer);
        return future.thenApplyAsync(
                response -> {
                    if (response instanceof TakeSnapshotResponse takeSnapshotResponse) {
                        log.info("takesnapshot-----------------------");
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

    private void sendRequest(
            CompletableFuture<Response> future,
            final long correlationId,
            final int length,
            final DirectBuffer sendBuffer) {
        ingressRequests.offer(new IngressRequest(future, correlationId, length, sendBuffer));
        log.debug("AERON REQUEST QUEUED correlationId={}, queueSize={}", correlationId, ingressRequests.size());
    }

    private int sendQueuedRequests() {
        int workCount = 0;
        for (int i = 0; i < 64; i++) {
            final IngressRequest request = ingressRequests.peek();
            if (request == null) {
                break;
            }

            long result = aeronCluster.offer(request.buffer(), 0, request.length());
            if (result == Publication.ADMIN_ACTION || result == Publication.BACK_PRESSURED) {
                break;
            }

            if (result == Publication.CLOSED || result == Publication.NOT_CONNECTED) {
                log.warn(
                        "AERON RECONNECT correlationId={}, offerResult={}",
                        request.correlationId(),
                        describeOfferResult(result)
                );
                reconnect();
                result = aeronCluster.offer(request.buffer(), 0, request.length());
            }

            ingressRequests.poll();
            if (result < 0) {
                pendingRequests.remove(request.correlationId(), request.future());
                request.future().completeExceptionally(new IllegalStateException("offer failed: " + describeOfferResult(result)));

                log.error("AERON REQUEST FAILED correlationId={}, offerResult={}", request.correlationId(), describeOfferResult(result));
            } else {
                log.debug("AERON REQUEST SENT correlationId={}, position={}", request.correlationId(), result);
                workCount++;
            }
        }
        return workCount;
    }


    @Override
    public void onMessage(
            final long clusterSessionId,
            final long timestamp,
            final DirectBuffer buffer,
            final int offset,
            final int length,
            final Header header) {


        final MessageHeaderDecoder messageHeaderDecoder = new MessageHeaderDecoder();

        messageHeaderDecoder.wrap(buffer, offset);

        final int headerLength = messageHeaderDecoder.encodedLength();
        final int actingBlockLength = messageHeaderDecoder.blockLength();
        final int actingVersion = messageHeaderDecoder.version();
        final MarketDepthDecoder marketDepthDecoder = new MarketDepthDecoder();
        final OrderInfoDecoder orderInfoDecoder = new OrderInfoDecoder();
        final ErrorMessageDecoder errorMessageDecoder = new ErrorMessageDecoder();
        final TakeSnapShotResponseDecoder takeSnapShotResponseDecoder = new TakeSnapShotResponseDecoder();
        log.info("AERON INCOMING " +
                "templateId=" + messageHeaderDecoder.templateId() +
                ", blockLength=" + messageHeaderDecoder.blockLength() +
                ", version=" + messageHeaderDecoder.version() +
                ", offset=" + offset +
                ", length=" + length);

        switch (messageHeaderDecoder.templateId()) {
            case OrderInfoDecoder.TEMPLATE_ID: {

                orderInfoDecoder.wrap(buffer, offset + headerLength, actingBlockLength, actingVersion);

                long correlationId = orderInfoDecoder.correlationId();
                long orderId = orderInfoDecoder.orderId();
                final CompletableFuture<Response> future = pendingRequests.remove(correlationId);

                log.info("PUT ORDER RESPONSE orderId={}, correlationId={}, futureFound={}, pending={}",
                        orderId, correlationId, future != null, pendingRequests.size());

                if (future != null) {
                    future.complete(new OrderInfoResponse(
                             orderId, orderInfoDecoder.timestamp(), orderInfoDecoder.userId(),
                            orderInfoDecoder.matchStatus(), orderInfoDecoder.filledQuantity()));
                }

                break;
            }

            case MarketDepthDecoder.TEMPLATE_ID: {
                marketDepthDecoder.wrap(buffer, offset + headerLength, actingBlockLength, actingVersion);

                long correlationId = marketDepthDecoder.correlationId();
                final CompletableFuture<Response> future = pendingRequests.remove(marketDepthDecoder.correlationId());

                log.info("PENDING SIZE=" + pendingRequests.size() +
                        "RESPONSE correlationId=" + correlationId +
                        ", futureFound=" + (future != null) +
                        ", pendingSize=" + pendingRequests.size());

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

                    future.complete(new OrderBookDepthResponse(bidsList, asksList));
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

    public int getPendingSize() {
        return pendingRequests.size();
    }

    public Set<Long> getPendingCorrelationIds() {
        return Set.copyOf(pendingRequests.keySet());
    }
}
