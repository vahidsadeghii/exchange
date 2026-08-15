package com.exchange.coregateway;

import com.exchange.core.sbe.MarketType;
import com.exchange.core.sbe.OrderType;
import com.exchange.core.sbe.TradePair;
import com.exchange.core.sbe.TradeSide;
import com.exchange.coregateway.service.MatchingEngineService;
import com.exchange.coresdk.domain.OrderInfoResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    public OrderInfoResponse getOrderInfo(@RequestParam("id") long orderId, @RequestParam("pair") String tradePair) {
        return matchingEngineService.getOrder(orderId, TradePair.valueOf(tradePair));
    }

    @PostMapping("/orders")
    public OrderInfoResponse putOrder(@RequestBody PutOrderRequest request) {
        return matchingEngineService.putOrder(
                request.orderId,
                request.userId,
                request.tradeSide,
                request.orderType,
                request.pair,
                request.marketType,
                request.quantity,
                request.price
        );
    }

    public record PutOrderRequest(long orderId, long userId, TradeSide tradeSide, OrderType orderType,
                                  TradePair pair, MarketType marketType, long quantity, long price) {
    }
}
