package com.exchange.coregateway.service;

import com.exchange.core.sbe.MarketType;
import com.exchange.core.sbe.OrderType;
import com.exchange.core.sbe.TradePair;
import com.exchange.core.sbe.TradeSide;
import com.exchange.coresdk.Client;
import com.exchange.coresdk.domain.OrderInfoResponse;
import org.springframework.stereotype.Service;

@Service
public class MatchingEngineService {
    private final Client client;

    public MatchingEngineService(Client client) {
        this.client = client;
    }

    public OrderInfoResponse getOrder(long orderId, TradePair tradePair) {

        try {
            OrderInfoResponse response = client.getOrder(orderId, tradePair).get();
            return response;
        } catch (Exception e) {
            e.printStackTrace();
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
                            10,
                            10)
                    .get();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
