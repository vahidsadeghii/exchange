package com.exchange.oms.controller.order.saveorder;

import com.exchange.oms.domain.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.exchange.oms.service.OrderService;

@RestController("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public CreateOrderResponse createOrder(@RequestBody CreateOrderRequest request) {
        //TODO: check the userID
        Order order = orderService.createOrder(
                1L,
                request.tradePair(),
                request.orderType(),
                request.quantity(),
                request.price());

        return new CreateOrderResponse(
                order.getId(),
                order.getTradePair().name(),
                order.getOrderType().name(),
                order.getStatus().name(),
                order.getQuantity(),
                order.getPrice(),
                order.getCreatedAt()
        );
    }
}
