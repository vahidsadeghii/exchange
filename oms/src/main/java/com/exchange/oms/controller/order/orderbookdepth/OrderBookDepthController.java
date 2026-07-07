package com.exchange.oms.controller.order.orderbookdepth;

import com.exchange.oms.domain.OrderBookDepth;
import com.exchange.oms.domain.TradePair;
import com.exchange.oms.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OrderBookDepthController {
        private final OrderService orderService;

    @GetMapping(value = "${api.prefix.internal}/orderdepth")
    public OrderBookDepthResponse getDepth(
            @RequestParam TradePair pair,
            @RequestParam(defaultValue = "10") int depth) {

        OrderBookDepth response = orderService.getOrderBookDepth(pair, depth);

        return new OrderBookDepthResponse(
                response.bids(),
                response.asks()

        );
    }
}
