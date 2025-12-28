package com.exchange.oms.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    @Enumerated(EnumType.STRING)
    private OrderType orderType;
    @Enumerated(EnumType.STRING)
    private TradeSide tradeSide;
    @Enumerated(EnumType.STRING)
    private TradePair tradePair;
    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.NEW;
    private double quantity;
    private double price;
    private MatchEventStatus matchEngineStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
