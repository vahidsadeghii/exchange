package com.exchange.me.controller.orderbookdepth;


import com.exchange.me.domain.TradePair;
import com.exchange.me.handler.OrderBookHandler;
import com.exchange.me.service.EngineService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class OrderBookDepthController {
    private final EngineService engineService;

    @GetMapping(value = "${api.prefix.internal}/orders")
    public OrderBookDepthResponse getDepth(
            @RequestParam TradePair pair,
            @RequestParam(defaultValue = "10") int depth) {

        OrderBookHandler book = engineService.getOrderBook(pair);

        List<PriceLevel> bids = book.getBidsList(depth)
                .stream()
                .map(p -> new PriceLevel(p.price(), p.volume(), p.orderCount()))
                .toList();

        List<PriceLevel> asks = book.getAsksList(depth)
                .stream()
                .map(p -> new PriceLevel(p.price(), p.volume(), p.orderCount()))
                .toList();

        return new OrderBookDepthResponse(bids, asks);
    }
}
