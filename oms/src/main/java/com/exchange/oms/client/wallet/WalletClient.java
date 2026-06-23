package com.exchange.oms.client.wallet;


import com.exchange.oms.config.exception.FallBackException;
import com.exchange.oms.config.feign.FeignException;
import com.exchange.oms.domain.AssetType;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@FeignClient(name = "wallet")
public interface WalletClient {
    Logger logger = LoggerFactory.getLogger(WalletClient.class);

    private Throwable parseThrowable(Throwable t) {
        logger.error("Error Server: " + t.getMessage());
        if (t instanceof FeignException)
            return t;
        else
            return new FallBackException();
    }


    @CircuitBreaker(name = "oms-instance", fallbackMethod = "createOrderMatchingEngineFallBack")
    @PostMapping(value = "/api/${api.prefix.internal}/order")
    BigDecimal findUserWalletBalance(@RequestParam Long userId, @RequestParam AssetType assetType);

    default void findUserWalletBalanceFallBack(@RequestParam Long userId, @RequestParam AssetType assetType, Throwable t) throws Throwable {
        throw parseThrowable(t);
    }
}
