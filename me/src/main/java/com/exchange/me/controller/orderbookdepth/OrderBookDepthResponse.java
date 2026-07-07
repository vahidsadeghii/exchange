package com.exchange.me.controller.orderbookdepth;

import java.util.List;

public record OrderBookDepthResponse(List<PriceLevel> bids,
                                     List<PriceLevel> asks) {
}
