package com.exchange.oms.service.decorator;


import com.exchange.oms.domain.*;
import com.exchange.oms.exception.order.*;
import com.exchange.oms.repository.OrderRepository;
import com.exchange.oms.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@RequiredArgsConstructor
@Service
@Transactional
@Primary
@Slf4j
public class OrderServiceDecorator implements OrderService {
    private final OrderService orderService;
    private final OrderRepository orderRepository;

    @Override
    public Order createUpdateOrder(Long oldOrderId,
                                   Long onlineUser,
                                   AssetType assetType,
                                   TradePair tradePair,
                                   TradeSide tradeSide,
                                   MarketType marketType, OrderType orderType,
                                   BigDecimal quantity,
                                   BigDecimal price) {

        if (onlineUser == null) {
            throw new MissingUserIdException();
        }

        if (assetType == null) {
            throw new MissingAssetTypeException();
        }

        if (tradePair == null) {
            throw new InvalidTradPairException();
        }

        if (tradeSide == null) {
            throw new InvalidTradSideException();
        }

        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidQuantityException();
        }

        // Price validation only for LIMIT orders
        if (orderType == OrderType.LIMIT &&
                (price == null || price.compareTo(BigDecimal.ZERO) <= 0)) {
            throw new InvalidPriceException();
        }

        return orderService.createUpdateOrder(
                oldOrderId,
                onlineUser,
                assetType,
                tradePair,
                tradeSide,
                marketType,
                orderType,
                quantity,
                price);
    }

    @Override
    public Order getOrder(long orderId) {
        if (orderId == 0L) {
            throw new MissingOrderIdException();
        }
        return orderService.getOrder(orderId);
    }


}
