package com.exchange.profile.client.email;


import com.exchange.profile.config.exception.FallBackException;
import com.exchange.profile.config.feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@FeignClient(name = "emailmanager")
public interface EmailmanagerClient {

    Logger logger = LoggerFactory.getLogger(EmailmanagerClient.class);

    private Throwable parseThrowable(Throwable t) {
        logger.error("Error Happend: " + t.getMessage());

        if (t instanceof FeignException)
            return t;
        else
            return new FallBackException();
    }


    @CircuitBreaker(name = "profile-instance", fallbackMethod = "VerifyEmailFallBack")
    @GetMapping(value = "/internal/verify-email")
    void VerifyEmail(@PathVariable String phoneNumber, @PathVariable String tanNumber);

    default void VerifyEmailallBack(@PathVariable String phoneNumber,
                                                                      @PathVariable String  tanNumber,
                                                                      Throwable t) throws Throwable {
        throw parseThrowable(t);
    }

}
