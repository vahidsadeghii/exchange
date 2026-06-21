package com.exchange.oms.controller.order.saveorder;

import com.exchange.oms.config.security.OnlineUser;
import com.exchange.oms.domain.Order;
import com.exchange.oms.exception.order.MissingUserIdException;
import io.micrometer.common.util.StringUtils;
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

    @PostMapping(value = "/orders")
     @PreAuthorize("hasRole('CUSTOMER')")
    public CreateOrderResponse createOrder(@AuthenticationPrincipal OnlineUser onlineUser,
                                           @RequestBody CreateOrderRequest request) {
        //TODO: check the userID
        // I plan to validate the userId after adding security to the application.
        // The following check is only a prototype.

        if (StringUtils.isEmpty(onlineUser.getUsername())) {
            throw new MissingUserIdException();
        }
        Order order = orderService.createOrder(
                onlineUser.getKeycloakUserId(),
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
