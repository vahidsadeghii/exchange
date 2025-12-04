package com.exchange.me.domain;

import lombok.*;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchInfo {
  private long executionId;
  private long timestamp;
  private OrderSide makerSide;
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
