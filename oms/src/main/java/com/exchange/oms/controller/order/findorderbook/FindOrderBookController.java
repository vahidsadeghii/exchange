package com.exchange.oms.controller.order.findorderbook;

import com.exchange.oms.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
public class FindOrderBookController {
    private final OrderService orderService;

    @GetMapping("${api.prefix.secure}/orderbook")
    @PreAuthorize("hasRole('CUSTOMER')")
    public void getOrderBook(@RequestParam Long orderId) {

        orderService.getOrder(orderId);
    }
}
