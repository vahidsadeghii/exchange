package com.exchange.oms.service.impl;

import com.exchange.oms.client.matchingengine.MatchingInfoClient;
import com.exchange.oms.client.matchingengine.CreateUpdateOrderRequestClient;
import com.exchange.oms.client.wallet.WalletClient;
import com.exchange.oms.config.exception.NotFoundException;
import com.exchange.oms.domain.*;
import com.exchange.oms.exception.order.InsufficientBalanceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.exchange.oms.repository.OrderRepository;
import com.exchange.oms.service.OrderService;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final MatchingInfoClient matchingEngineClient;
    private final WalletClient walletClient;


    @Override
    public Order createUpdateOrder(Long oldOrderId, Long onlineUser, AssetType assetType,
                                   TradePair tradePair, TradeSide tradeSide,
                                   MarketType marketType, OrderType orderType, BigDecimal quantity, BigDecimal price) {


        if (oldOrderId != null) {
            Order oldOrder = orderRepository
                    .findByIdAndStatus(oldOrderId, OrderStatus.NEW)
                    .orElseThrow(NotFoundException::new);

            oldOrder.setStatus(OrderStatus.CANCELED);
            orderRepository.save(oldOrder);
        }

        validateSufficientBalance(onlineUser, assetType, quantity, price);

        Order order = orderRepository.save(Order.builder()
                .userId(onlineUser)
                .tradePair(tradePair)
                .orderType(orderType)
                .tradeSide(tradeSide)
                .marketType(marketType)
                .status(OrderStatus.NEW)
                .quantity(quantity)
                .price(price)
                .createdAt(LocalDateTime.now())
                .build());

        MatchEngineResponse orderMatchingEngine = matchingEngineClient.createOrderMatchingEngine(
                new CreateUpdateOrderRequestClient(
                        oldOrderId,
                        order.getId(),
                        order.getUserId(),
                        order.getTradePair(),
                        order.getOrderType(),
                        order.getTradeSide(),
                        order.getMarketType(),
                        order.getQuantity().doubleValue(),
                        order.getPrice().doubleValue()));

        order.setMatchEngineStatus(orderMatchingEngine.status());
        return order;
    }

    @Override
    public Order getOrder(long orderId) {
        return null;
    }

    private void validateSufficientBalance(Long userId,
                                           AssetType assetType,
                                           BigDecimal quantity,
                                           BigDecimal price) {

        BigDecimal userWalletBalance = walletClient.findUserWalletBalance(userId, assetType);

        BigDecimal orderValue = quantity.multiply(price);

        if (userWalletBalance == null ||
                userWalletBalance.compareTo(orderValue) < 0) {

            throw new InsufficientBalanceException();
        }
    }


}
