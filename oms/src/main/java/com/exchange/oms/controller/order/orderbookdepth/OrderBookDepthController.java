package com.exchange.oms.controller.order.orderbookdepth;

import com.exchange.oms.domain.OrderBookDepth;
import com.exchange.oms.domain.TradePair;
import com.exchange.oms.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OrderBookDepthController {
    private final OrderService orderService;

    @GetMapping("/api/v1/orderdepth")
    @PreAuthorize("hasRole('CUSTOMER')")
    public OrderBookDepthResponse handle(
            @RequestParam TradePair pair,
            @RequestParam(defaultValue = "10") int depth) {

        OrderBookDepth response = orderService.getOrderBookDepth(pair, depth);

        return new OrderBookDepthResponse(
                response.bids(),
                response.asks()

        );
    }
}
