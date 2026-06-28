package com.exchange.oms.client.wallet;


import com.exchange.oms.config.exception.FallBackException;
import com.exchange.oms.config.feign.FeignException;
import com.exchange.oms.domain.AssetType;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@FeignClient(name = "wallet", url = "http://localhost:8092/wallet")
public interface WalletClient {
    Logger logger = LoggerFactory.getLogger(WalletClient.class);

    private Throwable parseThrowable(Throwable t) {
        logger.error("Error Server: " + t.getMessage());
        if (t instanceof FeignException)
            return t;
        else
            return new FallBackException();
    }


    @CircuitBreaker(name = "wallet-instance", fallbackMethod = "findUserWalletBalanceFallBack")
    @GetMapping(value = "/_api/v1/wallet-balance")
    @Retry(name = "wallet-instance")
    BigDecimal findUserWalletBalance(@RequestParam Long userId, @RequestParam AssetType assetType);

    default BigDecimal findUserWalletBalanceFallBack(@RequestParam Long userId, @RequestParam AssetType assetType, Throwable t) {
        logger.error(
                "Wallet service unavailable. userId={}, assetType={}",
                userId, assetType,
                t
        );
        return BigDecimal.ZERO;
    }
}
