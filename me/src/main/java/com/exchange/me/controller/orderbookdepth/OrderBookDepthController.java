package com.exchange.me.controller.orderbookdepth;


import com.exchange.me.domain.OrderBookDepth;
import com.exchange.me.domain.TradePair;
import com.exchange.me.service.EngineService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
public class OrderBookDepthController {
    private final EngineService engineService;

    @GetMapping(value = "${api.prefix.internal}/orders")
    public OrderBookDepthResponse getDepth(
            @RequestParam TradePair pair,
            @RequestParam(defaultValue = "10") int depth) {

        OrderBookDepth order = engineService.getOrderBookDepth(pair, depth);

        return new OrderBookDepthResponse(order.bids(), order.asks());
    }
}
