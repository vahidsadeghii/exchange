package com.exchange.oms.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

import java.math.BigDecimal;
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

    private BigDecimal quantity;

    private BigDecimal price;

    private MatchEventStatus matchEngineStatus;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
