package com.exchange.oms.client.matchingengine;


import com.exchange.oms.config.feign.FeignConfig;
import com.exchange.oms.controller.order.findorderbook.OrderBookResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


@FeignClient(
        name = "me",
        url = "http://localhost:8094/me",
        configuration = FeignConfig.class
)
public interface MatchingInfoClient {

    Logger log = LoggerFactory.getLogger(MatchingInfoClient.class);

    @PostMapping("/_api/v1/order")
    @CircuitBreaker(name = "me-instance", fallbackMethod = "createOrderFallback")
    @Retry(name = "me-instance")
    void createOrderMatchingEngine(@RequestBody CreateOrderRequest request);

    default void createOrderFallback(CreateOrderRequest request, Throwable t) {
        log.warn("ME unavailable - order dropped orderId={}", request.orderId(), t);
    }



    @GetMapping("/_api/v1/order-book")
    @CircuitBreaker(name = "me-instance", fallbackMethod = "getOrderBookFallback")
    @Retry(name = "me-instance")
    OrderBookResponse getOrderBook(@RequestParam long orderId);

    default OrderBookResponse getOrderBookFallback(long orderId, Throwable t) {
         log.warn("ME unavailable - orderbook fallback orderId={}", orderId, t);

        OrderBookResponse build = OrderBookResponse.builder()
                .bids(List.of())
                .asks(List.of())
                .userOrders(List.of())
                .updateTime(System.currentTimeMillis())
                .build();
        return build;
    }
}