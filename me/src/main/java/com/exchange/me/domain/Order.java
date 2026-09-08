package com.exchange.me.domain;

import com.exchange.me.sbe.*;

import lombok.*;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    private long id;
    private long timestamp;
    private long userId;
    private TradeSide tradeSide;
    private OrderType orderType;
    private TradePair tradePair;
    private MarketType  marketType;
    private MatchStatus matchStatus;
    private long quantity;
    private long price;
    private long filled;
    private long expireDays;


    public long getRemainingQuantity() {
        return quantity - filled;
    }
}
