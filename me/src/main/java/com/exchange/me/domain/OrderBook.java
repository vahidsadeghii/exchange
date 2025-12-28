package com.exchange.me.domain;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderBook {
    private List<Order> bids;
    private List<Order> asks;
    private List<Order> userOrders;
    private LocalDateTime updateTime;
}
