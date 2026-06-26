package com.exchange.oms.controller.order.saveorder;

import com.exchange.oms.config.security.OnlineUser;
import com.exchange.oms.domain.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.exchange.oms.service.OrderService;

@RestController
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping(value = "${api.prefix.secure}/orders")
     @PreAuthorize("hasRole('CUSTOMER')")
    public CreateOrderResponse createOrder(@AuthenticationPrincipal OnlineUser onlineUser,
                                           @RequestBody CreateOrderRequest request) {

        Order order = orderService.createOrder(
                onlineUser.getInternalUserId(),
                request.assetType(),
                request.tradePair(),
                request.tradeSide(),
                request.orderType(),
                request.quantity(), request.price());

        return new CreateOrderResponse(
                order.getId(),
                order.getTradePair().name(),
                order.getTradeSide().name(),
                order.getOrderType().name(),
                order.getStatus().name(),
                order.getQuantity(),
                order.getPrice(),
                order.getCreatedAt()
        );
    }
}
