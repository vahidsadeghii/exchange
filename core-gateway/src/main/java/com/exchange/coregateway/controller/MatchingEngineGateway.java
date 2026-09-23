package com.exchange.coregateway.controller;

import com.exchange.coresdk.domain.TakeSnapshotResponse;
import com.exchange.me.sbe.*;
import com.exchange.coregateway.service.MatchingEngineService;
import com.exchange.coresdk.domain.OrderBookDepthResponse;
import com.exchange.coresdk.domain.OrderInfoResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me")
@Slf4j
public class MatchingEngineGateway {
    private final MatchingEngineService matchingEngineService;

    public MatchingEngineGateway(MatchingEngineService matchingEngineService) {
        this.matchingEngineService = matchingEngineService;
    }

    @GetMapping("/orders")
    public OrderInfoResponse getOrderInfo(@RequestParam("id") long orderId, @RequestParam("pair") String tradePair) {
        return matchingEngineService.getOrder(orderId, com.exchange.me.sbe.TradePair.valueOf(tradePair));
    }

    @PostMapping("/orders")
    public OrderInfoResponse putOrder(@RequestBody PutOrderRequest request) {
        long start = System.nanoTime();

        try {
            log.info("putOrder START orderId={}", request.orderId);

            OrderInfoResponse response = matchingEngineService.putOrder(
                    request.orderId,
                    request.userId,
                    request.tradeSide,
                    request.orderType,
                    request.pair,
                    request.marketType,
                    request.quantity,
                    request.price
            );

            long durationMs = (System.nanoTime() - start) / 1_000_000;

            log.info(
                    "putOrder END orderId={}, duration={} ms, responseId={}",
                    request.orderId,
                    durationMs,
                    response != null ? response.getId() : null
            );

            return response;

        } catch (Exception e) {
            long durationMs = (System.nanoTime() - start) / 1_000_000;

            log.error(
                    "putOrder FAILED orderId={}, duration={} ms",
                    request.orderId,
                    durationMs,
                    e
            );

            throw e;
        }
    }

    @DeleteMapping("/orders")
    public OrderInfoResponse cancelOrder(@RequestParam("id") long orderId, @RequestParam("pair") String tradePair) {
        return matchingEngineService.cancelOrder(orderId, com.exchange.me.sbe.TradePair.valueOf(tradePair));
    }

    @GetMapping("/orderdepth")
    public OrderBookDepthResponse orderDepth(@RequestParam("pair") String pair, @RequestParam("depth") int depth) {
        return matchingEngineService.orderBookDepth(com.exchange.me.sbe.TradePair.valueOf(pair), depth);
    }

    @GetMapping("/takesnapshot")
    public TakeSnapshotResponse takeSnapShot() {
        return matchingEngineService.takeSnapShot();
    }

    public record PutOrderRequest(long orderId, long userId, TradeSide tradeSide, OrderType orderType,
                                  TradePair pair, MarketType marketType, long quantity, long price) {
    }
}
