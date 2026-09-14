package com.exchange.me.domain;

import com.exchange.me.sbe.TradeSide;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchInfo {
    private String id;
    private long executionId;
    private long timestamp;
    private TradeSide makerSide;
    private long takerOrderId;
    private long makerOrderId;
    private long takerUserId;
    private long makerUserId;
    private double quantity;
    private double price;
    private double takerOriginalQuantity;
    private double takerRemain;
    private double makerOriginalQuantity;
    private double makerRemain;
}
