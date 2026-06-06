package com.exchange.profile.client.wallet;


import com.exchange.profile.config.exception.FallBackException;
import com.exchange.profile.config.feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


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

    @CircuitBreaker(name = "wallet-instance", fallbackMethod = "createWalletFallBack")
    @PostMapping("/_api/v1/user/wallet")
    void createWallet(@RequestParam("userId") Long userId);

    default void createWalletFallBack(Long userId, Throwable t) {
        logger.error("Wallet service failed for userId=" + userId, t);
    }
}
