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
    private double quantity;
    private double price;
    private double filled;

    public double getRemainingQuantity() {
        return quantity - filled;
    }
}
