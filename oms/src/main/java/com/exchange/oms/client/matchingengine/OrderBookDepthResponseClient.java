package com.exchange.oms.client.matchingengine;

import com.exchange.oms.domain.PriceLevel;

import java.util.List;

public record OrderBookDepthResponseClient(List<PriceLevel> bids,
                                           List<PriceLevel> asks) {
}
