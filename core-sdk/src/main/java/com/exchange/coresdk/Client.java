package com.exchange.coresdk;

import com.exchange.core.sbe.ErrorMessageDecoder;
import com.exchange.core.sbe.GetOrderInfoEncoder;
import com.exchange.core.sbe.MarketType;
import com.exchange.core.sbe.MatchStatus;
import com.exchange.core.sbe.MessageHeaderDecoder;
import com.exchange.core.sbe.MessageHeaderEncoder;
import com.exchange.core.sbe.OrderInfoDecoder;
import com.exchange.core.sbe.OrderType;
import com.exchange.core.sbe.PutOrderEncoder;
import com.exchange.core.sbe.TradePair;
import com.exchange.core.sbe.TradeSide;
import com.exchange.coresdk.domain.OrderInfoResponse;
import com.exchange.coresdk.domain.Response;
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

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
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

    private final MessageHeaderDecoder messageHeaderDecoder = new MessageHeaderDecoder();
    private final OrderInfoDecoder orderInfoDecoder = new OrderInfoDecoder();

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

        sendRequest(future,
                correlationId, messageHeaderEncoder.encodedLength() + putOrderEncoder.encodedLength());

        return future.thenApply(
                response -> {
                    if (response instanceof OrderInfoResponse orderInfoResponse) {
                        return orderInfoResponse;
                    } else {
                        return new OrderInfoResponse(response.getErrorCode());
                    }
                }
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
                correlationId, messageHeaderEncoder.encodedLength() + getOrderInfoEncoder.encodedLength());

        return future.thenApply(
                response -> {
                    if (response instanceof OrderInfoResponse orderInfoResponse) {
                        return orderInfoResponse;
                    } else {
                        return new OrderInfoResponse(response.getErrorCode());
                    }
                }
        );
    }

    private long nextCorrelationId() {
        return correlationIdSequence.incrementAndGet();
    }

    private void sendRequest(CompletableFuture<Response> future, final long correlationId, final int length) {
        long result = offer(length);

        if (result == Publication.CLOSED || result == Publication.NOT_CONNECTED) {
            // The cluster connection died (e.g. the cluster node was restarted) - reconnect once
            // and retry before giving up.
            reconnect();
            result = offer(length);
        }

        if (result < 0) {
            System.out.println("Error on sending request: " + result);
            pendingRequests.remove(correlationId);
            future.completeExceptionally(
                    new IllegalStateException("offer failed: " + describeOfferResult(result)));
        } else {
            System.out.println("Success on sending request: " + result);
        }
    }

    private long offer(final int length) {
        long result;
        do {
            result = aeronCluster.offer(sendBuffer, 0, length);
        } while (result == Publication.ADMIN_ACTION || result == Publication.BACK_PRESSURED);

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

        System.out.println("New result coming: " + messageHeaderDecoder.templateId());

        switch (messageHeaderDecoder.templateId()) {
            case OrderInfoDecoder.TEMPLATE_ID: {
                orderInfoDecoder.wrap(buffer, offset + headerLength, actingBlockLength, actingVersion);

                final CompletableFuture<Response> future =
                        pendingRequests.remove(orderInfoDecoder.correlationId());
                if (future != null) {
                    future.complete(
                            new OrderInfoResponse(
                                    orderInfoDecoder.orderId(),
                                    orderInfoDecoder.timestamp(),
                                    orderInfoDecoder.userId(),
                                    orderInfoDecoder.matchStatus(),
                                    orderInfoDecoder.filledQuantity()));
                }
                break;
            }

            case ErrorMessageDecoder.TEMPLATE_ID: {
                errorMessageDecoder.wrap(buffer, offset + headerLength, actingBlockLength, actingVersion);
                final CompletableFuture<Response> future =
                        pendingRequests.remove(orderInfoDecoder.correlationId());
                if (future != null) {
                    future.complete(
                            new Response(errorMessageDecoder.code()));
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
