package com.exchange.oms.controller.order;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exchange.oms.service.OrderService;

@RestController("/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public void createOrder(CreateOrderRequest request) {
        // Implementation for creating an order goes here
        orderService.createOrder(
            request.tradePair(),
            request.orderType(),
            request.quantity(),
            request.price()
        );
    }

    @GetMapping("/orderbook")
    public void getOrderBook() {
        // Implementation for retrieving order book goes here
    }
}
