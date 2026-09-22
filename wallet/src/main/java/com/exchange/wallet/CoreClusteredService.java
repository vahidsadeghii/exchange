package com.exchange.wallet;


import com.exchange.wallet.sbe.MessageHeaderDecoder;
import com.exchange.wallet.service.RequestFunction;
import io.aeron.ExclusivePublication;
import io.aeron.Image;
import io.aeron.cluster.codecs.CloseReason;
import io.aeron.cluster.service.ClientSession;
import io.aeron.cluster.service.Cluster;
import io.aeron.cluster.service.ClusteredService;

import io.aeron.logbuffer.Header;
import lombok.extern.slf4j.Slf4j;
import org.agrona.DirectBuffer;
import org.agrona.ExpandableDirectByteBuffer;

import java.util.*;


@Slf4j
public class CoreClusteredService implements ClusteredService {
    private final MessageHeaderDecoder messageHeaderDecoder;
    private final ExpandableDirectByteBuffer respondBuffer;

    private final ExpandableDirectByteBuffer snapshotBuffer;

    private final HashMap<Integer, RequestFunction> requestMap;
    private Cluster cluster;


    public CoreClusteredService() {
        log.info("Initializing CoreClusteredService");

        messageHeaderDecoder = new MessageHeaderDecoder();
        this.respondBuffer = new ExpandableDirectByteBuffer(1024);
        snapshotBuffer = new ExpandableDirectByteBuffer(1024 * 1024);

        requestMap = new HashMap<>();
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

    }

}