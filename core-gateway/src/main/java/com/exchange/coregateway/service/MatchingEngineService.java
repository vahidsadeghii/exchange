package com.exchange.coregateway.service;

import com.exchange.core.sbe.MarketType;
import com.exchange.core.sbe.OrderType;
import com.exchange.core.sbe.TradePair;
import com.exchange.core.sbe.TradeSide;
import com.exchange.coresdk.Client;
import com.exchange.coresdk.domain.OrderBookDepthResponse;
import com.exchange.coresdk.domain.OrderInfoResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class MatchingEngineService {
    private final Client client;

    private final Logger logger = LoggerFactory.getLogger(MatchingEngineService.class);

    public MatchingEngineService(Client client) {
        this.client = client;
    }

    public OrderInfoResponse getOrder(long orderId, TradePair tradePair) {
        try {
            return client.getOrder(orderId, tradePair).get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.error("Error while fetching order {}, {}", orderId, tradePair, e);
            return null;
        }
    }

    public OrderInfoResponse cancelOrder(long orderId, TradePair tradePair) {
        try {
            return client.cancelOrder(orderId, tradePair).get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.error("Error while cancel order {}, {}", orderId, tradePair, e);
            return null;
        }
    }

    public OrderBookDepthResponse orderBookDepth(TradePair pair, int depth) {
        try {
            return client.getOrderBookDepth(pair, depth).get(5, TimeUnit.SECONDS);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            logger.error("Interrupted while fetching order book depth {}, {}", depth, pair, ie);
            return null;
        } catch (TimeoutException te) {
            logger.warn("Timeout while fetching order book depth {}, {} (timeout 5s)", depth, pair, te);
            return null;
        } catch (ExecutionException ee) {
            Throwable cause = ee.getCause();
            if (cause != null && cause instanceof io.aeron.exceptions.ConductorServiceTimeoutException) {
                logger.error("Aeron conductor timeout (fatal) while fetching order book depth {}, {}. Cause: {}",
                        depth, pair, cause.toString(), cause);
                // consider escalation / alerting here
            } else {
                logger.error("Execution error while fetching order book depth {}, {}", depth, pair, ee);
            }
            return null;
        } catch (Exception e) {
            logger.error("Unexpected error while fetching order book depth {}, {}", depth, pair, e);
            return null;
        }
    }

    public OrderInfoResponse putOrder(long orderId, long userId, TradeSide tradeSide,
                                      OrderType orderType, TradePair tradePair, MarketType marketType,
                                      long quantity, long price) {
        try {
            return client
                    .putOrder(orderId, System.currentTimeMillis(), userId, tradeSide, orderType, tradePair,
                            marketType, quantity, price)
                    .get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.error("Error while new order {}, {}", orderId, tradePair, e);
            return null;
        }
    }
}
