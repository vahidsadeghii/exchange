package com.exchange.oms.controller.order.findorderbook;

import com.exchange.oms.domain.Order;
import lombok.*;

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
    private long updateTime;
}
