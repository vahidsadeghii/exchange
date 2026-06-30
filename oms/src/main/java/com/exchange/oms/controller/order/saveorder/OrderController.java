package com.exchange.oms.controller.order.saveorder;

import com.exchange.oms.config.security.OnlineUser;
import com.exchange.oms.controller.order.CreateUpdateOrderRequest;
import com.exchange.oms.controller.order.CreateUpdateOrderResponse;
import com.exchange.oms.domain.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.exchange.oms.service.OrderService;

@RestController
@RequiredArgsConstructor
@Slf4j
public class OrderController {
    private final OrderService orderService;

    @PostMapping(value = "${api.prefix.secure}/orders")
    @PreAuthorize("hasRole('CUSTOMER')")
    public CreateUpdateOrderResponse createOrder(@AuthenticationPrincipal OnlineUser onlineUser,
                                                 @RequestBody CreateUpdateOrderRequest request) {
        log.info("request = {}", request);
        log.info("oldOrderId = {}", request.oldOrderId());
        Order order = orderService.createUpdateOrder(request.oldOrderId(),
                onlineUser.getInternalUserId(),
                request.assetType(),
                request.tradePair(),
                request.tradeSide(), ,
                request.orderType(), request.quantity(), request.price());

        return new CreateUpdateOrderResponse(
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
