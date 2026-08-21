package com.exchange.coregateway.service;

import com.exchange.core.sbe.MarketType;
import com.exchange.core.sbe.OrderType;
import com.exchange.core.sbe.TradePair;
import com.exchange.core.sbe.TradeSide;
import com.exchange.coresdk.Client;
import com.exchange.coresdk.domain.OrderInfoResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

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

    public OrderInfoResponse putOrder(
            long orderId,
            long userId,
            TradeSide tradeSide,
            OrderType orderType,
            TradePair tradePair,
            MarketType marketType,
            long quantity,
            long price) {
        try {
            return client
                    .putOrder(
                            orderId,
                            System.currentTimeMillis(),
                            userId,
                            tradeSide,
                            orderType,
                            tradePair,
                            marketType,
                            quantity,
                            price)
                    .get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.error("Error while new order {}, {}", orderId, tradePair, e);
            return null;
        }
    }
}
