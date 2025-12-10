package com.exchange.me.controller.findorderbook;

import com.exchange.me.domain.Order;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;


@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderBookResponse {
    private List<Order> bids;
    private List<Order> asks;
    private List<Order> userOrders;
    private LocalDateTime updateTime;
}
