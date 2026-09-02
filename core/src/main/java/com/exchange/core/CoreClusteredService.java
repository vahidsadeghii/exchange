package com.exchange.core;

import com.exchange.core.sbe.*;
import com.exchange.core.service.RequestFunction;
import com.exchange.core.service.RequestHandlerService;
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

    private final HashMap<Integer, RequestFunction> requestMap;
    private final ExpandableDirectByteBuffer respondBuffer;

    public CoreClusteredService() {
        RequestHandlerService requestHandlerService = new RequestHandlerService();

        messageHeaderDecoder = new MessageHeaderDecoder();
        this.respondBuffer = new ExpandableDirectByteBuffer(1024);

        requestMap = new HashMap<>();

        requestMap.put(PutOrderDecoder.TEMPLATE_ID, requestHandlerService::handlePutOrderRequest);
        requestMap.put(GetOrderInfoDecoder.TEMPLATE_ID, requestHandlerService::handleGetOrderInfo);
        requestMap.put(CancelOrderDecoder.TEMPLATE_ID, requestHandlerService::handleCancelOrder);
        requestMap.put(OrderBookDepthDecoder.TEMPLATE_ID, requestHandlerService::handleOrderBookDepth);
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

        System.out.println("Received templateId=" + templateId);

        messageHeaderDecoder.wrap(buffer, offset);

        System.out.println(
                "Expected=" + OrderBookDepthDecoder.TEMPLATE_ID +
                        ", Received=" + templateId);

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

        long result;
        do {
            result = session.offer(respondBuffer, 0, responseLen);
        } while (result == io.aeron.Publication.ADMIN_ACTION
                || result == io.aeron.Publication.BACK_PRESSURED);

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

}
