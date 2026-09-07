package com.exchange.me_core.domain;

import com.exchange.core.sbe.MarketType;
import com.exchange.core.sbe.MatchStatus;
import com.exchange.core.sbe.OrderType;
import com.exchange.core.sbe.TradePair;
import com.exchange.core.sbe.TradeSide;
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
