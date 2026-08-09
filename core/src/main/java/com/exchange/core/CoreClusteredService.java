package com.exchange.core;

import com.exchange.core.sbe.MatchStatus;
import com.exchange.core.sbe.MessageHeaderDecoder;
import com.exchange.core.sbe.MessageHeaderEncoder;
import com.exchange.core.sbe.OrderInfoEncoder;
import com.exchange.core.sbe.PutOrderDecoder;
import com.exchange.me.service.EngineService;
import io.aeron.ExclusivePublication;
import io.aeron.Image;
import io.aeron.cluster.codecs.CloseReason;
import io.aeron.cluster.service.ClientSession;
import io.aeron.cluster.service.Cluster;
import io.aeron.cluster.service.ClusteredService;
import io.aeron.logbuffer.Header;
import java.util.HashMap;
import org.agrona.DirectBuffer;
import org.agrona.ExpandableDirectByteBuffer;

public class CoreClusteredService implements ClusteredService {

  private Cluster cluster;
  private final MessageHeaderDecoder messageHeaderDecoder;
  private final MessageHeaderEncoder messageHeaderEncoder;

  private final PutOrderDecoder putOrderDecoder;
  private final OrderInfoEncoder orderInfoEncoder;

  private HashMap<Integer, RequestFunction> requestMap;

  private final EngineService engineServiceImpl;

  private final ExpandableDirectByteBuffer respondBuffer;

  public CoreClusteredService() {
    this.engineServiceImpl = new EngineService();

    this.respondBuffer = new ExpandableDirectByteBuffer(1024);
    this.messageHeaderDecoder = new MessageHeaderDecoder();
    this.messageHeaderEncoder = new MessageHeaderEncoder();
    this.putOrderDecoder = new PutOrderDecoder();
    this.orderInfoEncoder = new OrderInfoEncoder();

    requestMap = new HashMap<>();

    requestMap.put(PutOrderDecoder.TEMPLATE_ID, this::handlePutOrderRequest);
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

  private int handlePutOrderRequest(
      long sessionId,
      long timestamp,
      DirectBuffer buffer,
      int offset,
      int headerLength,
      int actingLength,
      int actingVersion,
      ExpandableDirectByteBuffer respondBuffer) {
    putOrderDecoder.wrap(respondBuffer, offset, actingLength, actingVersion);

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

    orderInfoEncoder
        .wrapAndApplyHeader(respondBuffer, offset, messageHeaderEncoder)
        .correlationId(putOrderDecoder.correlationId())
        .orderId(order.getId())
        .timestamp(order.getTimestamp())
        .userId(order.getUserId())
        .matchStatus(MatchStatus.SUBMITED)
        .filledQuantity((long) order.getQuantity() - (long) order.getRemainingQuantity());

    return orderInfoEncoder.encodedLength() + messageHeaderDecoder.encodedLength();
  }
}
