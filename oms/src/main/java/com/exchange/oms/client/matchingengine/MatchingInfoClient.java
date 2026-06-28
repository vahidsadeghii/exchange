package com.exchange.oms.client.matchingengine;


import com.exchange.oms.client.wallet.WalletClient;
import com.exchange.oms.config.exception.FallBackException;
import com.exchange.oms.config.feign.FeignException;
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
        name = "matchingengine",
        url = "http://localhost:8094/matchingengine"
)
public interface MatchingInfoClient {

    Logger logger = LoggerFactory.getLogger(WalletClient.class);

    private Throwable parseThrowable(Throwable t) {
        logger.error("Error Server: " + t.getMessage());
        if (t instanceof FeignException)
            return t;
        else
            return new FallBackException();
    }

    @PostMapping("/_api/v1/order")
    @CircuitBreaker(name = "me-instance", fallbackMethod = "createOrderMatchingEngineFallback")
    @Retry(name = "me-instance")
    void createOrderMatchingEngine(@RequestBody NewOrderRequest request);

    default void createOrderMatchingEngineFallback(@RequestBody NewOrderRequest request, Throwable t) {
        logger.error(
                "ME service unavailable, order dropped. orderId={}",
                request.orderId(),
                t
        );

    }


    @GetMapping("/_api/v1/order-book")
    @CircuitBreaker(name = "me-instance", fallbackMethod = "getOrderBookFallback")
    @Retry(name = "me-instance")
    OrderBookResponse getOrderBook(@RequestParam long orderId);

    default OrderBookResponse getOrderBookFallback(long orderId, Throwable t) {
        logger.error("ME unavailable, returning empty order book. orderId={}", orderId, t);

        return null;
    }
}