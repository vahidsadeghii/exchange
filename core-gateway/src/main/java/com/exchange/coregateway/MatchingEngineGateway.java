package com.exchange.coregateway;

import com.exchange.core.sbe.MarketType;
import com.exchange.core.sbe.OrderType;
import com.exchange.core.sbe.TradePair;
import com.exchange.core.sbe.TradeSide;
import com.exchange.coregateway.service.MatchingEngineService;
import com.exchange.coresdk.domain.OrderInfoResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me")
public class MatchingEngineGateway {
    private final MatchingEngineService matchingEngineService;

    public MatchingEngineGateway(MatchingEngineService matchingEngineService) {
        this.matchingEngineService = matchingEngineService;
    }

    @GetMapping("/orders")
    public OrderInfoResponse getOrderInfo(@RequestParam("id") long orderId) {
        return matchingEngineService.getOrder(orderId, TradePair.BTC_EURO);
    }

    @PostMapping("/orders")
    public OrderInfoResponse putOrder() {
        return matchingEngineService.putOrder(
                1, 1, TradeSide.BUY, OrderType.MARKET, TradePair.BTC_EURO, MarketType.SPOT, 10, 10);
    }
}
