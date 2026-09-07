package com.exchange.coresdk.domain;

import com.exchange.me.sbe.MatchStatus;

public class OrderInfoResponse extends Response {
    private long id;
    private long timestamp;
    private long userId;
    private MatchStatus matchStatus;
    private long filledQuantity;

    public OrderInfoResponse(int errorCode) {
        super(errorCode);

    }

    public OrderInfoResponse(long id, long timestamp, long userId, MatchStatus matchStatus, long filledQuantity) {
        super(0);
        this.id = id;
        this.timestamp = timestamp;
        this.userId = userId;
        this.matchStatus = matchStatus;
        this.filledQuantity = filledQuantity;
    }

    public long getId() {
        return id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getUserId() {
        return userId;
    }

    public MatchStatus getMatchStatus() {
        return matchStatus;
    }

    public long getFilledQuantity() {
        return filledQuantity;
    }
}
