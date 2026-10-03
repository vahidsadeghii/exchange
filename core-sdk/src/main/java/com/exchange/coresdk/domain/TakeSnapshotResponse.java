package com.exchange.coresdk.domain;

public class TakeSnapshotResponse extends Response {
    private long correlationId;

    public TakeSnapshotResponse(int errorCode) {
        super(errorCode);
    }

    private TakeSnapshotResponse(long correlationId) {
        super(0);
        this.correlationId = correlationId;
    }

    public long getCorrelationId() {
        return correlationId;
    }
}
