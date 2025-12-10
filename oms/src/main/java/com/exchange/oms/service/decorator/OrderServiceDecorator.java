package com.exchange.oms.service.decorator;


import com.exchange.oms.controller.order.findorderbook.OrderBookResponse;
import com.exchange.oms.domain.*;
import com.exchange.oms.exception.order.*;
import com.exchange.oms.repository.OrderRepository;
import com.exchange.oms.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional
@Primary
@Slf4j
public class OrderServiceDecorator implements OrderService {
    private final OrderService orderService;
    private final OrderRepository orderRepository;



    @Override
    public Order createOrder(long userId, TradePair tradePair, OrderType orderType, double quantity, double price) {
        if(userId == 0L) {
            throw new MissingUserIdException();
        }
        if (tradePair == null) {
            throw new InvalidTradPairException();
        }
        if (quantity <= 0) {
            throw new InvalidQuantityException();
        }
        if (price <= 0) {
            throw new InvalidPriceException();
        }
        return orderService.createOrder(userId, tradePair, orderType, quantity, price);
    }

    @Override
    public Order updateOrder(long orderId, long userId, MatchEngineStatus orderStatus) {
        if(userId == 0L) {
            throw new MissingUserIdException();
        }
        if(orderId == 0L) {
            throw new MissingOrderIdException();
        }

        return orderService.updateOrder(userId, orderId, orderStatus);
    }

    @Override
    public OrderBookResponse getOrder(long orderId) {
        if(orderId == 0L) {
            throw new MissingOrderIdException();
        }
        return orderService.getOrder(orderId);
    }


}
