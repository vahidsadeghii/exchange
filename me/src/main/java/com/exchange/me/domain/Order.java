package com.exchange.me.domain;

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
    private MatchEventStatus matchEngineStatus;
    private double quantity;
    private double price;
    private double filled;
    private Long expireDays;


    public double getRemainingQuantity() {
        return quantity - filled;
    }
}
