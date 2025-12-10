package com.exchange.oms.client.matchingengine;


import com.exchange.oms.config.exception.FallBackException;
import com.exchange.oms.config.feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "em")
public interface MatchingInfoClient {
    Logger logger = LoggerFactory.getLogger(MatchingInfoClient.class);

    private Throwable parseThrowable(Throwable t) {
        logger.error("Error Server: " + t.getMessage());
        if (t instanceof FeignException)
            return t;
        else
            return new FallBackException();
    }


    @CircuitBreaker(name="oms-instance", fallbackMethod = "saveOrderMatchingEngineFallBack")
    @PostMapping(value = "/api/${api.prefix.internal}/match-info")
    void saveOrderMatchingEngine(@RequestBody saveOrderRequest updateOrderRequest);

    default void saveOrderMatchingEngineFallBack(@RequestBody saveOrderRequest request, Throwable t) throws Throwable{
        throw parseThrowable(t);
    }

    @CircuitBreaker(name="oms-instance", fallbackMethod = "getOrderBook")
    @PostMapping(value = "/api/${api.prefix.internal}/order")
    void getOrderBook(@RequestParam long orderId);

    default void getOrderBook(@RequestParam long orderId, Throwable t) throws Throwable{
        throw parseThrowable(t);
    }


}
