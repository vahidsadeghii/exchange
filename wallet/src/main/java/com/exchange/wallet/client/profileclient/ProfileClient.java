package com.exchange.wallet.client.profileclient;

import com.exchange.wallet.config.exception.FallBackException;
import com.exchange.wallet.config.feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;


@FeignClient(name = "profile", url = "http://localhost:8088")
public interface ProfileClient {
    Logger logger = LoggerFactory.getLogger(ProfileClient.class);

    private Throwable parseThrowable(Throwable t) {
        logger.error("Error Server: " + t.getMessage());

        if (t instanceof FeignException)
            return t;

        else
            return new FallBackException();
    }

    @CircuitBreaker(name = "wallet-instance", fallbackMethod = "findUserByKeycloakIdFallBack")
    @GetMapping("/profile/open/user-id")
    Optional<Long> findUserByKeycloakId(@RequestParam("keycloakId") String keycloakId);


    default Optional<Long> findUserByKeycloakIdFallBack(String keycloakId, Throwable t) {
        return Optional.empty();
    }


}
