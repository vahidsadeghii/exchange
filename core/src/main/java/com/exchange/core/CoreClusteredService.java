package com.exchange.core;

import com.exchange.core.config.ErrorCode;
import com.exchange.core.sbe.ErrorMessageEncoder;
import com.exchange.core.sbe.GetOrderInfoDecoder;
import com.exchange.core.sbe.MessageHeaderDecoder;
import com.exchange.core.sbe.MessageHeaderEncoder;
import com.exchange.core.sbe.OrderInfoEncoder;
import com.exchange.core.sbe.PutOrderDecoder;
import com.exchange.me.domain.Order;
import com.exchange.me.service.EngineService;
import io.aeron.ExclusivePublication;
import io.aeron.Image;
import io.aeron.cluster.codecs.CloseReason;
import io.aeron.cluster.service.ClientSession;
import io.aeron.cluster.service.Cluster;
import io.aeron.cluster.service.ClusteredService;
import io.aeron.logbuffer.Header;
import org.agrona.DirectBuffer;
import org.agrona.ExpandableDirectByteBuffer;

import java.util.HashMap;

public class CoreClusteredService implements ClusteredService {

    private Cluster cluster;

    private final MessageHeaderDecoder messageHeaderDecoder;
    private final MessageHeaderEncoder messageHeaderEncoder;

    private final ErrorMessageEncoder errorMessageEncoder;

    private final PutOrderDecoder putOrderDecoder;
    private final OrderInfoEncoder orderInfoEncoder;

    private final GetOrderInfoDecoder getOrderInfoDecoder;

    private final HashMap<Integer, RequestFunction> requestMap;

    private final EngineService engineServiceImpl;

    private final ExpandableDirectByteBuffer respondBuffer;

    public CoreClusteredService() {
        this.engineServiceImpl = new EngineService();

        this.respondBuffer = new ExpandableDirectByteBuffer(1024);

        // Encoder-Decoders
        this.messageHeaderDecoder = new MessageHeaderDecoder();
        this.messageHeaderEncoder = new MessageHeaderEncoder();
        this.errorMessageEncoder = new ErrorMessageEncoder();
        this.putOrderDecoder = new PutOrderDecoder();
        this.orderInfoEncoder = new OrderInfoEncoder();
        this.getOrderInfoDecoder = new GetOrderInfoDecoder();

        requestMap = new HashMap<>();

        requestMap.put(PutOrderDecoder.TEMPLATE_ID, this::handlePutOrderRequest);
        requestMap.put(GetOrderInfoDecoder.TEMPLATE_ID, this::handleGetOrderInfo);
    }

    @Override
    public void onStart(final Cluster cluster, final Image snapshotImage) {
        this.cluster = cluster;
        System.out.println("Cluster node started, role=" + cluster.role());
    }

    @Override
    public void onSessionOpen(final ClientSession session, final long timestamp) {
        System.out.println("Session opened: id=" + session.id());
    }

    @Override
    public void onSessionClose(
            final ClientSession session, final long timestamp, final CloseReason closeReason) {
        System.out.println("Session closed: id=" + session.id() + ", reason=" + closeReason);
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
            return;
        }

        messageHeaderDecoder.wrap(buffer, offset);
        int templateId = messageHeaderDecoder.templateId();

        final int headerLength = messageHeaderDecoder.encodedLength();
        final int actingLength = messageHeaderDecoder.blockLength();
        final int actingVersion = messageHeaderDecoder.version();

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

        System.out.println("Response len: " + responseLen);
        long result;
        do {
            result = session.offer(respondBuffer, 0, responseLen);
        } while (result == io.aeron.Publication.ADMIN_ACTION
                || result == io.aeron.Publication.BACK_PRESSURED);

        System.out.println("Successfully offer response: " + result);
    }

    @Override
    public void onTimerEvent(final long correlationId, final long timestamp) {
        System.out.println("Timer fired: correlationId=" + correlationId);
    }

    @Override
    public void onTakeSnapshot(final ExclusivePublication snapshotPublication) {
        // No state to persist yet.
    }

    @Override
    public void onRoleChange(final Cluster.Role newRole) {
        System.out.println("Role changed to " + newRole);
    }

    @Override
    public void onTerminate(final Cluster cluster) {
        System.out.println("Cluster node terminating");
    }

    private int handlePutOrderRequest(
            long sessionId,
            long timestamp,
            DirectBuffer buffer,
            int offset,
            int headerLength,
            int actingLength,
            int actingVersion,
            ExpandableDirectByteBuffer respondBuffer) {
        putOrderDecoder.wrap(buffer, offset + headerLength, actingLength, actingVersion);

        System.out.println("Put order called: " + putOrderDecoder.orderId());
        var order =
                engineServiceImpl.createUpdateOrder(
                        null,
                        putOrderDecoder.orderId(),
                        putOrderDecoder.userId(),
                        putOrderDecoder.tradeSide(),
                        putOrderDecoder.tradePair(),
                        putOrderDecoder.orderType(),
                        putOrderDecoder.marketType(),
                        putOrderDecoder.quantity(),
                        putOrderDecoder.price());

        if (order != null) {
            orderInfoEncoder
                    .wrapAndApplyHeader(respondBuffer, 0, messageHeaderEncoder)
                    .correlationId(putOrderDecoder.correlationId())
                    .orderId(order.getId())
                    .timestamp(order.getTimestamp())
                    .userId(order.getUserId())
                    .matchStatus(order.getMatchStatus())
                    .filledQuantity((long) order.getQuantity() - (long) order.getRemainingQuantity());

            return orderInfoEncoder.encodedLength() + messageHeaderDecoder.encodedLength();
        } else {
            return returnErrorMessage(respondBuffer, putOrderDecoder.correlationId(), ErrorCode.ORDER_NOT_FOUND);
        }
    }

    private int handleGetOrderInfo(
            long sessionId,
            long timestamp,
            DirectBuffer buffer,
            int offset,
            int headerLength,
            int actingLength,
            int actingVersion,
            ExpandableDirectByteBuffer respondBuffer) {
        getOrderInfoDecoder.wrap(buffer, offset + headerLength, actingLength, actingVersion);

        System.out.println("Get order info called: " + getOrderInfoDecoder.orderId());

        Order order;
        try {
            order =
                    engineServiceImpl.getOrder(getOrderInfoDecoder.tradePair(), getOrderInfoDecoder.orderId());
        } catch (Exception e) {
            order = null;
        }

        if (order != null) {
            orderInfoEncoder
                    .wrapAndApplyHeader(respondBuffer, 0, messageHeaderEncoder)
                    .correlationId(putOrderDecoder.correlationId())
                    .orderId(order.getId())
                    .timestamp(order.getTimestamp())
                    .userId(order.getUserId())
                    .matchStatus(order.getMatchStatus())
                    .filledQuantity((long) order.getQuantity() - (long) order.getRemainingQuantity());

            return orderInfoEncoder.encodedLength() + messageHeaderDecoder.encodedLength();
        } else {
            int len = returnErrorMessage(respondBuffer, putOrderDecoder.correlationId(), ErrorCode.ORDER_NOT_FOUND);
            System.out.println("Make error message: " + len);
            return len;
        }
    }

    private int returnErrorMessage(ExpandableDirectByteBuffer respondBuffer, long correlationId, int errorCode) {
        System.out.println("Wrap header");
        errorMessageEncoder.wrapAndApplyHeader(
                respondBuffer, 0, messageHeaderEncoder
        );
        errorMessageEncoder.correlationId(correlationId);
        errorMessageEncoder.code(errorCode);

        int len = errorMessageEncoder.encodedLength() + messageHeaderDecoder.encodedLength();
        System.out.println("Return len: " + len);
        return len;
    }
}
