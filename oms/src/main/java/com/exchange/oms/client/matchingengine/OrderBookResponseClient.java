package com.exchange.oms.client.matchingengine;

import com.exchange.oms.domain.Order;
import lombok.*;

import java.util.List;


@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderBookResponseClient {
    private List<Order> bids;
    private List<Order> asks;
    private List<Order> userOrders;
    private long updateTime;
}
