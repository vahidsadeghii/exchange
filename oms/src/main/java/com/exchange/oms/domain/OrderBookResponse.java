package com.exchange.oms.domain;

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
}
