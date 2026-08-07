package com.exchange.core;

import io.aeron.ExclusivePublication;
import io.aeron.Image;
import io.aeron.cluster.codecs.CloseReason;
import io.aeron.cluster.service.Cluster;
import io.aeron.cluster.service.ClientSession;
import io.aeron.cluster.service.ClusteredService;
import io.aeron.logbuffer.Header;
import org.agrona.DirectBuffer;

public class CoreClusteredService implements ClusteredService {

  private Cluster cluster;

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

    long result;
    do {
      result = session.offer(buffer, offset, length);
    } while (result == io.aeron.Publication.ADMIN_ACTION || result == io.aeron.Publication.BACK_PRESSURED);
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
