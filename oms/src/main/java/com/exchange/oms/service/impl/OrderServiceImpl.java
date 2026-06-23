package com.exchange.oms.service.impl;

import com.exchange.oms.client.matchingengine.MatchingInfoClient;
import com.exchange.oms.client.matchingengine.createOrderRequest;
import com.exchange.oms.client.wallet.WalletClient;
import com.exchange.oms.controller.order.findorderbook.OrderBookResponse;
import com.exchange.oms.domain.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.exchange.oms.repository.OrderRepository;
import com.exchange.oms.service.OrderService;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final MatchingInfoClient matchingEngineClient;
    private final WalletClient walletClient;


    @Override
    public Order createOrder(String keycloakUserId, TradePair tradePair, TradeSide tradeSide, OrderType orderType, double quantity, double price) {

        Order order = orderRepository.save(Order.builder()
                .keycloakUserId(keycloakUserId)
                .tradePair(tradePair)
                .orderType(orderType)
                .tradeSide(tradeSide)
                .status(OrderStatus.NEW)
                .quantity(quantity)
                .price(price)
                .createdAt(LocalDateTime.now())
                .build());

        matchingEngineClient.createOrderMatchingEngine(
                new createOrderRequest(
                        order.getId(),
                      1L,
                        order.getTradePair(),
                        order.getOrderType(),
                        order.getTradeSide(),
                        order.getQuantity(),
                        order.getPrice()));

        return order;
    }

    //TODO: find by orderId oder userId
    // Currently, findById uses orderId.
    // Consider whether userId is a better practice for locating an order.
    @Override
    public Order updateOrder(long orderId, long userId, MatchEventStatus orderStatus) {
        return orderRepository.findByUserId(userId)
                .map(order -> {
                    order.setMatchEngineStatus(orderStatus);
                    return orderRepository.save(order);
                })
                .orElseThrow(() -> new EntityNotFoundException("Order not found with userId: " + userId));
    }

    @Override
    public OrderBookResponse getOrder(long orderId) {
        return null;
    }


}
