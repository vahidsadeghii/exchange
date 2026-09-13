package com.exchange.me;


import com.exchange.me.domain.EngineSnapshot;
import com.exchange.me.domain.Order;
import com.exchange.me.sbe.*;
import com.exchange.me.service.EngineService;
import com.exchange.me.service.RequestFunction;
import com.exchange.me.service.RequestHandlerService;
import io.aeron.ExclusivePublication;
import io.aeron.Image;
import io.aeron.cluster.codecs.CloseReason;
import io.aeron.cluster.codecs.MessageHeaderDecoder;
import io.aeron.cluster.service.ClientSession;
import io.aeron.cluster.service.Cluster;
import io.aeron.cluster.service.ClusteredService;
import io.aeron.logbuffer.FragmentHandler;
import io.aeron.logbuffer.Header;
import lombok.extern.slf4j.Slf4j;
import org.agrona.DirectBuffer;
import org.agrona.ExpandableDirectByteBuffer;
import io.aeron.Publication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


@Slf4j
public class CoreClusteredService implements ClusteredService {
    private final MessageHeaderDecoder messageHeaderDecoder;
    private final ExpandableDirectByteBuffer respondBuffer;
    private final EngineSnapshotEncoder snapshotEncoder;
    private final EngineService engineService;
    private final MessageHeaderEncoder messageHeaderEncoder;
    private final ExpandableDirectByteBuffer snapshotBuffer;
    private final EngineSnapshotDecoder snapshotDecoder;

    private final HashMap<Integer, RequestFunction> requestMap;
    private Cluster cluster;


    public CoreClusteredService() {
        log.info("Initializing CoreClusteredService");

        RequestHandlerService requestHandlerService = new RequestHandlerService();
        engineService = new EngineService();
        messageHeaderDecoder = new MessageHeaderDecoder();
        this.snapshotEncoder = new EngineSnapshotEncoder();
        this.snapshotDecoder = new EngineSnapshotDecoder();
        this.respondBuffer = new ExpandableDirectByteBuffer(1024);
        messageHeaderEncoder = new MessageHeaderEncoder();
        snapshotBuffer = new ExpandableDirectByteBuffer(1024 * 1024);

        requestMap = new HashMap<>();
        requestMap.put(PutOrderDecoder.TEMPLATE_ID, requestHandlerService::handlePutOrderRequest);
        requestMap.put(GetOrderInfoDecoder.TEMPLATE_ID, requestHandlerService::handleGetOrderInfo);
        requestMap.put(CancelOrderDecoder.TEMPLATE_ID, requestHandlerService::handleCancelOrder);
        requestMap.put(OrderBookDepthDecoder.TEMPLATE_ID, requestHandlerService::handleOrderBookDepth);

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

        if (session == null) {
            log.warn("Received message with null session: offset={}, length={}", offset, length);
            return;
        }

        messageHeaderDecoder.wrap(buffer, offset);
        int templateId = messageHeaderDecoder.templateId();

        log.debug("Received templateId=" + templateId);

        messageHeaderDecoder.wrap(buffer, offset);

        log.debug("Expected=" + OrderBookDepthDecoder.TEMPLATE_ID + ", Received=" + templateId);

        final int headerLength = messageHeaderDecoder.encodedLength();
        final int actingLength = messageHeaderDecoder.blockLength();
        final int actingVersion = messageHeaderDecoder.version();

        log.debug("Received cluster message: sessionId={}, templateId={}, timestamp={}, offset={}, length={}, headerLength={}, blockLength={}, version={}",
                session.id(),
                templateId,
                timestamp,
                offset,
                length,
                headerLength,
                actingLength,
                actingVersion);

        int responseLen =
                requestMap
                        .get(templateId)
                        .handleRequest(
                                session.id(),
                                timestamp,
                                buffer,
                                offset,
                                headerLength,
                                actingLength,
                                actingVersion,
                                respondBuffer);

        log.debug("Request handled successfully: sessionId={}, templateId={}, responseLength={}",
                session.id(),
                templateId,
                responseLen);

        long result;
        do {
            result = session.offer(respondBuffer, 0, responseLen);
            if (result == io.aeron.Publication.ADMIN_ACTION) {
                log.debug("Session offer returned ADMIN_ACTION: sessionId={}, templateId={}",
                        session.id(),
                        templateId);
            } else if (result == io.aeron.Publication.BACK_PRESSURED) {
                log.debug("Session offer back pressured: sessionId={}, templateId={}",
                        session.id(),
                        templateId);
            }

        } while (result == io.aeron.Publication.ADMIN_ACTION
                || result == io.aeron.Publication.BACK_PRESSURED);

        if (result < 0) {
            log.error("Failed to send response: sessionId={}, templateId={}, result={}",
                    session.id(),
                    templateId,
                    result);
        } else {
            log.debug("Response sent successfully: sessionId={}, templateId={}, result={}",
                    session.id(),
                    templateId,
                    result);
        }

    }

    @Override
    public void onTimerEvent(final long correlationId, final long timestamp) {
        log.debug("Cluster timer fired: correlationId={}, timestamp={}", correlationId, timestamp);
    }

    @Override
    public void onTakeSnapshot(final ExclusivePublication snapshotPublication) {

        System.out.println("!!!!!!!!!! SNAPSHOT CALLBACK !!!!!!!!!!!");

        log.info("========== TAKING SNAPSHOT ==========");
        log.info("Starting engine snapshot");

        EngineSnapshot snapshot = engineService.createSnapshot();

        log.info("Engine snapshot created: orderCount={}", snapshot.orders().size());

        snapshotEncoder.wrapAndApplyHeader(snapshotBuffer, 0, messageHeaderEncoder);

        EngineSnapshotEncoder.OrdersEncoder ordersEncoder = snapshotEncoder.ordersCount(snapshot.orders().size());

        for (Order order : snapshot.orders()) {
            ordersEncoder
                    .next()
                    .orderId(order.getId())
                    .timestamp(order.getTimestamp())
                    .userId(order.getUserId())
                    .tradeSide(TradeSide.valueOf(order.getTradeSide().name()))
                    .orderType(OrderType.valueOf(order.getOrderType().name()))
                    .tradePair(TradePair.valueOf(order.getTradePair().name()))
                    .marketType(MarketType.valueOf(order.getMarketType().name()))
                    .matchStatus(MatchStatus.valueOf(order.getMatchStatus().name()))
                    .quantity(order.getQuantity())
                    .price(order.getPrice())
                    .filled(order.getFilled())
                    .expireDays(order.getExpireDays());
        }

        int length = messageHeaderEncoder.encodedLength() + snapshotEncoder.encodedLength();

        log.info("Snapshot encoded: orderCount={}, payloadLength={}", snapshot.orders().size(), length);

        while (true) {
            long result = snapshotPublication.offer(snapshotBuffer, 0, length);

            if (result > 0) {
                break;
            }

            if (result == Publication.CLOSED || result == Publication.MAX_POSITION_EXCEEDED) {
                log.error("Snapshot publication failed: result={}, orderCount={}, length={}", result, snapshot.orders().size(), length);

                throw new IllegalStateException("Snapshot offer failed: " + result);
            }

            log.debug("========== TAKING SNAPSHOT ==========");
            log.debug("Snapshot publication temporarily unavailable: result={}, orderCount={}", result, snapshot.orders().size());

            Thread.yield();
        }
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
        log.info("Loading engine snapshot");

        List<Order> restoredOrders = new ArrayList<>();

        final FragmentHandler handler = (buffer, offset, length, header) -> {
            messageHeaderDecoder.wrap(buffer, offset);
            int templateId = messageHeaderDecoder.templateId();

            if (messageHeaderDecoder.templateId() != EngineSnapshotDecoder.TEMPLATE_ID) {
                log.warn("Ignoring unexpected snapshot template: templateId={}", templateId);
                return;
            }
            int headerLength = messageHeaderDecoder.encodedLength();
            snapshotDecoder.wrap(buffer, offset + headerLength, messageHeaderDecoder.blockLength(), messageHeaderDecoder.version()
            );

            EngineSnapshotDecoder.OrdersDecoder ordersDecoder = snapshotDecoder.orders();

            while (ordersDecoder.hasNext()) {
                ordersDecoder.next();
                Order order = Order.builder()
                        .id(ordersDecoder.orderId())
                        .userId(ordersDecoder.userId())
                        .timestamp(ordersDecoder.timestamp())
                        .tradeSide(ordersDecoder.tradeSide())
                        .tradePair(ordersDecoder.tradePair())
                        .orderType(ordersDecoder.orderType())
                        .marketType(ordersDecoder.marketType())
                        .matchStatus(ordersDecoder.matchStatus())
                        .quantity(ordersDecoder.quantity())
                        .price(ordersDecoder.price())
                        .filled(ordersDecoder.filled())
                        .expireDays(ordersDecoder.expireDays())
                        .build();

                restoredOrders.add(order);
                log.debug("Snapshot fragment processed: fragmentLength={}, restoredOrdersSoFar={}", length, restoredOrders.size());
            }
        };

        while (true) {
            int fragments = snapshotImage.poll(handler, 100);
            if (snapshotImage.isEndOfStream()) {
                log.info("Snapshot image reached end of stream: restoredOrderCount={}", restoredOrders.size());
                break;
            }
            cluster.idleStrategy().idle(fragments);
        }
        engineService.restoreOrders(restoredOrders);

        log.info("Engine snapshot restored successfully: orderCount={}", restoredOrders.size());
    }

}
