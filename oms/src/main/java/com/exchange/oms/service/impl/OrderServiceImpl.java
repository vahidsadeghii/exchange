package com.exchange.oms.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.exchange.oms.domain.Order;
import com.exchange.oms.domain.OrderType;
import com.exchange.oms.domain.TradePair;
import com.exchange.oms.repository.OrderRepository;
import com.exchange.oms.service.OrderService;

import jakarta.transaction.Transactional;

@Service
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;

    public OrderServiceImpl(@Autowired OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional
    @Override
    public Order createOrder(TradePair tradePair, OrderType orderType, double quantity, double price) {
        // Implementation logic to create and return an Order
        return orderRepository.save( new Order(tradePair, orderType, quantity, price));
    }
}
