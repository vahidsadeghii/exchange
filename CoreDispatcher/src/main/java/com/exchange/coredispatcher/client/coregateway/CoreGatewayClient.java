package com.exchange.coredispatcher.client.coregateway;

import com.exchange.coredispatcher.config.exception.FallBackException;
import com.exchange.coredispatcher.config.feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "core-gateway", url = "${core-gateway.url:http://localhost:8089}")
public interface CoreGatewayClient {
    Logger logger = LoggerFactory.getLogger(CoreGatewayClient.class);

    private Throwable parseThrowable(Throwable t) {
        logger.error("Error Happend: " + t.getMessage());

        if (t instanceof FeignException)
            return t;
        else
            return new FallBackException();
    }


    //@CircuitBreaker(name = "profile-instance", fallbackMethod = "VerifyEmailFallBack")
    @GetMapping("/api/v1/me/matchinfo")
    List<MatchInfoResponse> getMatchInfo(@RequestParam int maxMatches);

    default List<MatchInfoResponse> getMatchInfo(@RequestParam int maxMatches,
                                    Throwable t) throws Throwable {
        throw parseThrowable(t);
    }

}