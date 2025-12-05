package com.exchange.oms.service.impl;

import com.exchange.oms.client.matchingengine.MatchingEngineClient;
import com.exchange.oms.client.matchingengine.saveOrderRequest;
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

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final MatchingEngineClient matchingEngineClient;

    @Override
    public Order createOrder(long userId, TradePair tradePair, OrderType orderType, double quantity, double price) {
        //TODO: check the order properties
        //Property validation is handled in OrderServiceDecorator
        Order order = orderRepository.save(Order.builder()
                .userId(userId)
                .tradePair(tradePair)
                .orderType(orderType)
                .status(OrderStatus.NEW)
                .isBuyOrder(false)
                .quantity(quantity)
                .price(price)
                .createdAt(LocalDateTime.now())
                .build());

        matchingEngineClient.saveOrderMatchingEngine(new saveOrderRequest(
                order.getId(),
                order.getUserId(),
                order.getTradePair(),
                order.getOrderType(),
                order.getIsBuyOrder(),
                order.getQuantity(),
                order.getPrice()));

        return order;
    }

    //TODO: find by orderId oder userId
    // Currently, findById uses orderId.
    // Consider whether userId is a better practice for locating an order.
    @Override
    public Order updateOrder(long orderId, long userId, MatchEngineStatus orderStatus) {
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
