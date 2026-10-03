package com.exchange.oms.controller.order.cancelorder;


import com.exchange.oms.config.security.OnlineUser;
import com.exchange.oms.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;



@RestController
@RequiredArgsConstructor
public class CancelOrderController {
    private final OrderService orderService;
    private final OnlineUser onlineUser;


    @DeleteMapping(value = "${api.prefix.secure}/order")
    @PreAuthorize("hasRole('CUSTOMER')")
    public void handle(@RequestParam("id") long orderId, @RequestParam("pair") String tradePair){

    }
}
