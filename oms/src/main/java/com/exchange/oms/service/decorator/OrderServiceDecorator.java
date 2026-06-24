package com.exchange.oms.service.decorator;


import com.exchange.oms.controller.order.findorderbook.OrderBookResponse;
import com.exchange.oms.domain.*;
import com.exchange.oms.exception.order.*;
import com.exchange.oms.repository.OrderRepository;
import com.exchange.oms.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
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
    public Order createOrder(Long onlineUser, AssetType assetType, TradePair tradePair, TradeSide tradeSide, OrderType orderType, BigDecimal quantity, BigDecimal price) {
        if (onlineUser == null) {
            throw new MissingUserIdException();
        }

        if (assetType == null) {
            throw new MissingAssetTypeException();
        }

        if (StringUtils.isEmpty(tradePair.name())) {
            throw new InvalidTradPairException();
        }

        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidQuantityException();
        }

        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidPriceException();
        }

        if (StringUtils.isEmpty(tradeSide.name())) {
            throw new InvalidTradSideException();
        }

        return orderService.createOrder(onlineUser, assetType, tradePair, tradeSide, orderType, quantity, price);
    }

    @Override
    public Order updateOrder(long orderId, long userId, MatchEventStatus orderStatus) {
        if (userId == 0L) {
            throw new MissingUserIdException();
        }
        if (orderId == 0L) {
            throw new MissingOrderIdException();
        }

        return orderService.updateOrder(userId, orderId, orderStatus);
    }

    @Override
    public OrderBookResponse getOrder(long orderId) {
        if (orderId == 0L) {
            throw new MissingOrderIdException();
        }
        return orderService.getOrder(orderId);
    }


}
