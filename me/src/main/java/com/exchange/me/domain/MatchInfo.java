package com.exchange.me.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;



@Document
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchInfo {

    @Id
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
