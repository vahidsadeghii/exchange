package com.exchange.me.domain;

public class MatchInfo {
  long executionId;
  long timestamp;
  Order.OrderSide makerSide;
  long takerOrderId;
  long makerOrderId;
  long takerUserId;
  long makerUserId;
  double quantity;
  double price;
  double takerOriginalQuantity;
  double takerRemain;
  double makerOriginalQuantity;
  double makerRemain;

  public MatchInfo() {
  }

  public MatchInfo(long executionId, long timestamp, Order.OrderSide makerSide, long takerOrderId,
      long makerOrderId, long takerUserId, long makerUserId, double quantity,
      double price, double takerOriginalQuantity, double takerRemain,
      double makerOriginalQuantity, double makerRemain) {
    this.executionId = executionId;
    this.timestamp = timestamp;
    this.makerSide = makerSide;
    this.takerOrderId = takerOrderId;
    this.makerOrderId = makerOrderId;
    this.takerUserId = takerUserId;
    this.makerUserId = makerUserId;
    this.quantity = quantity;
    this.price = price;
    this.takerOriginalQuantity = takerOriginalQuantity;
    this.takerRemain = takerRemain;
    this.makerOriginalQuantity = makerOriginalQuantity;
    this.makerRemain = makerRemain;
  }

  public long getExecutionId() {
    return executionId;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public Order.OrderSide getMakerSide() {
    return makerSide;
  }

  public long getTakerOrderId() {
    return takerOrderId;
  }

  public long getMakerOrderId() {
    return makerOrderId;
  }

  public long getTakerUserId() {
    return takerUserId;
  }

  public long getMakerUserId() {
    return makerUserId;
  }

  public double getQuantity() {
    return quantity;
  }

  public double getPrice() {
    return price;
  }

  public double getTakerOriginalQuantity() {
    return takerOriginalQuantity;
  }

  public double getTakerRemain() {
    return takerRemain;
  }

  public double getMakerOriginalQuantity() {
    return makerOriginalQuantity;
  }

  public double getMakerRemain() {
    return makerRemain;
  }
}
