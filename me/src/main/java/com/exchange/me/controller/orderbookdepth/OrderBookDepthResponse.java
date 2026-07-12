package com.exchange.me.controller.orderbookdepth;

import com.exchange.me.domain.PriceLevel;

import java.util.List;

public record OrderBookDepthResponse(List<PriceLevel> bids,
                                     List<PriceLevel> asks) {
}
