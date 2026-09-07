package com.exchange.me.service;

import org.agrona.DirectBuffer;
import org.agrona.ExpandableDirectByteBuffer;

@FunctionalInterface
public interface RequestFunction {
    int handleRequest(long sessionId,
        long timestamp,
        DirectBuffer buffer,
        int offset,
        int headerLength,
        int actingLength,
        int actingVersion,
        ExpandableDirectByteBuffer respondBuffer);
}
