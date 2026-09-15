package com.exchange.me;


import com.exchange.me.domain.EngineSnapshot;
import com.exchange.me.domain.Order;
import com.exchange.me.sbe.CancelOrderDecoder;
import com.exchange.me.sbe.EngineSnapshotDecoder;
import com.exchange.me.sbe.GetOrderInfoDecoder;
import com.exchange.me.sbe.MarketType;
import com.exchange.me.sbe.MatchStatus;
import com.exchange.me.sbe.MessageHeaderEncoder;
import com.exchange.me.sbe.OrderBookDepthDecoder;
import com.exchange.me.sbe.OrderType;
import com.exchange.me.sbe.PutOrderDecoder;
import com.exchange.me.sbe.SnapshotOrderBookDecoder;
import com.exchange.me.sbe.SnapshotOrderBookEncoder;
import com.exchange.me.sbe.SnapshotOrderDecoder;
import com.exchange.me.sbe.SnapshotOrderEncoder;
import com.exchange.me.sbe.TradePair;
import com.exchange.me.sbe.TradeSide;
import com.exchange.me.service.EngineService;
import com.exchange.me.service.RequestFunction;
import com.exchange.me.service.RequestHandlerService;
import io.aeron.ExclusivePublication;
import io.aeron.Image;
import io.aeron.Publication;
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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.BiConsumer;


@Slf4j
public class CoreClusteredService implements ClusteredService {
    private final MessageHeaderDecoder messageHeaderDecoder;
    private final ExpandableDirectByteBuffer respondBuffer;
    private final SnapshotOrderEncoder snapshotOrderEncoder;
    private final SnapshotOrderDecoder snapshotOrderDecoder;
    private final SnapshotOrderBookEncoder snapshotOrderBookEncoder;
    private final SnapshotOrderBookDecoder snapshotOrderBookDecoder;
    private final EngineService engineService;
    private final MessageHeaderEncoder messageHeaderEncoder;
    private final ExpandableDirectByteBuffer snapshotBuffer;

    private final HashMap<Integer, RequestFunction> requestMap;
    private Cluster cluster;


    public CoreClusteredService() {
        log.info("Initializing CoreClusteredService");

        RequestHandlerService requestHandlerService = new RequestHandlerService();
        engineService = new EngineService();
        messageHeaderDecoder = new MessageHeaderDecoder();
        this.snapshotOrderEncoder = new SnapshotOrderEncoder();
        this.snapshotOrderBookEncoder = new SnapshotOrderBookEncoder();
        this.snapshotOrderBookDecoder = new SnapshotOrderBookDecoder();
        this.snapshotOrderDecoder = new SnapshotOrderDecoder();
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

        for (Order order : snapshot.orders()) {
            snapshotOrderEncoder.wrapAndApplyHeader(snapshotBuffer, 0, messageHeaderEncoder);
            snapshotOrderEncoder
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

            int length = messageHeaderEncoder.encodedLength() + snapshotOrderEncoder.encodedLength();
            offerSnapshot(snapshotPublication, length);
        }

        snapshot.bids().forEach(
                (pair, levels) -> writeBookSnapshot(
                        snapshotPublication,
                        pair,
                        TradeSide.BUY,
                        levels));

        snapshot.asks().forEach(
                (pair, levels) -> writeBookSnapshot(
                        snapshotPublication,
                        pair,
                        TradeSide.SELL,
                        levels));
    }

    private void writeBookSnapshot(ExclusivePublication publication, TradePair pair, TradeSide side, Map<Long, Deque<Order>> levels) {
        snapshotOrderBookEncoder.wrapAndApplyHeader(snapshotBuffer, 0, messageHeaderEncoder);

        SnapshotOrderBookEncoder.LevelEncoder levelEncoder =
                snapshotOrderBookEncoder.pair(pair).side(side).levelCount(levels.size());
        levels.forEach((price, orders) -> {
            var levelOrders = levelEncoder.next()
                    .price(price)
                    .levelOrdersCount(orders.size());

            orders.forEach(order ->
                    levelOrders.next()
                            .orderId(order.getId()));
        });

        int length = messageHeaderEncoder.encodedLength() + snapshotOrderBookEncoder.encodedLength();

        offerSnapshot(publication, length);
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
        Map<TradePair, Map<Long, Deque<Long>>> bids = new TreeMap<>();
        Map<TradePair, Map<Long, Deque<Long>>> asks = new TreeMap<>();

        final FragmentHandler handler = (buffer, offset, length, header) -> {
            messageHeaderDecoder.wrap(buffer, offset);
            int templateId = messageHeaderDecoder.templateId();

            if (templateId != SnapshotOrderDecoder.TEMPLATE_ID && templateId != SnapshotOrderBookDecoder.TEMPLATE_ID) {
                log.warn("Ignoring snapshot template {}", templateId);
                return;
            }


            int headerLength = messageHeaderDecoder.encodedLength();

            switch (templateId) {
                case SnapshotOrderDecoder.TEMPLATE_ID -> {
                    snapshotOrderDecoder.wrap(buffer, offset + headerLength, messageHeaderDecoder.blockLength(), messageHeaderDecoder.version());
                    Order order = Order.builder()
                            .id(snapshotOrderDecoder.orderId())
                            .userId(snapshotOrderDecoder.userId())
                            .timestamp(snapshotOrderDecoder.timestamp())
                            .tradeSide(snapshotOrderDecoder.tradeSide())
                            .tradePair(snapshotOrderDecoder.tradePair())
                            .orderType(snapshotOrderDecoder.orderType())
                            .marketType(snapshotOrderDecoder.marketType())
                            .matchStatus(snapshotOrderDecoder.matchStatus())
                            .quantity(snapshotOrderDecoder.quantity())
                            .price(snapshotOrderDecoder.price())
                            .filled(snapshotOrderDecoder.filled())
                            .expireDays(snapshotOrderDecoder.expireDays())
                            .build();

                    restoredOrders.add(order);
                    log.debug("Snapshot fragment processed: fragmentLength={}, restoredOrdersSoFar={}", length, restoredOrders.size());
                    break;
                }

                case SnapshotOrderBookDecoder.TEMPLATE_ID -> {
                    snapshotOrderBookDecoder.wrap(buffer, offset + headerLength, messageHeaderDecoder.blockLength(), messageHeaderDecoder.version());
                    TradePair pair = snapshotOrderBookDecoder.pair();
                    TradeSide side = snapshotOrderBookDecoder.side();
                    SnapshotOrderBookDecoder.LevelDecoder level = snapshotOrderBookDecoder.level();

                    while (level.hasNext()) {
                        SnapshotOrderBookDecoder.LevelDecoder levelDetail = level.next();
                        long price = levelDetail.price();
                        SnapshotOrderBookDecoder.LevelDecoder.LevelOrdersDecoder levelOrdersDecoder = levelDetail.levelOrders();

                        ArrayDeque<Long> orderIds = new ArrayDeque<>();
                        while (levelOrdersDecoder.hasNext()) {
                            orderIds.add(levelOrdersDecoder.next().orderId());
                        }

                        if (side == TradeSide.BUY) {
                            Objects.requireNonNull(bids.putIfAbsent(pair, new TreeMap<>())).putIfAbsent(price, orderIds);
                        } else {
                            Objects.requireNonNull(asks.putIfAbsent(pair, new TreeMap<>())).putIfAbsent(price, orderIds);
                        }
                    }
                }
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
        engineService.restoreOrderBooks(restoredOrders, bids, asks);

        log.info("Engine snapshot restored successfully: orderCount={}", restoredOrders.size());
    }

}
