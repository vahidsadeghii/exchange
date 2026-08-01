package com.exchange.oms.controller.order.orderbookdepth;

import com.exchange.oms.domain.PriceLevel;

import java.util.List;

public record OrderBookDepthResponse(List<PriceLevel> bids,
                                     List<PriceLevel> asks) {
}
