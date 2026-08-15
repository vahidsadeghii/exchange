package com.exchange.core;

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
