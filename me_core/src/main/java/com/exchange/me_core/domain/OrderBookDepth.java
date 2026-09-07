package com.exchange.me_core.domain;

import java.util.List;

public record OrderBookDepth(List<PriceLevel> bids,
                             List<PriceLevel> asks) {
}
