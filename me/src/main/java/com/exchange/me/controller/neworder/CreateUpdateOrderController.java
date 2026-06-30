package com.exchange.me.controller.neworder;


import com.exchange.me.domain.MatchEngine;
import com.exchange.me.service.EngineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class CreateUpdateOrderController {
    private final EngineService engineService;


    @PostMapping(value = "${api.prefix.internal}/order")
    public MatchEngineResponse handle(@RequestBody CreateUpdateOrderRequest request) {
        MatchEngine response = engineService.createUpdateOrder(request.oldOrderId(),
                request.orderId(), request.userId(),
                request.tradeSide(), request.tradePair(),
                request.orderType(), request.marketType(),
                request.quantity(), request.price());

        return new MatchEngineResponse(response.orderId(), response.userId(), response.status());
    }
}
