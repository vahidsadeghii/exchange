package com.exchange.me.controller.updatematchinfo;


import com.exchange.me.service.EngineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class SaveMatchInfoController {
    private final EngineService engineService;


    @PostMapping(value = "/api/${api.prefix.internal}/match-info")
    public void UpdateMatchInfoController(@RequestBody saveOrderRequest request) {
        engineService.processOrder(request.orderId(), request.userId(),
                request.tradePair(), request.orderType(),request.isBuyOrder(),
                request.quantity(), request.price());
    }
}