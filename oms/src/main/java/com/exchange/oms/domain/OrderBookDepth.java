package com.exchange.oms.domain;

import java.util.List;

public record OrderBookDepth(List<PriceLevel> bids,
                             List<PriceLevel> asks) {
}
