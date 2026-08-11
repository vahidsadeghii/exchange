package com.exchange.oms.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@Builder
@Table(name = "orders")
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

    @Enumerated(EnumType.STRING)
    private MarketType marketType;

    private BigDecimal quantity;

    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    private MatchEventStatus matchEngineStatus;

    private Long expireDays;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
