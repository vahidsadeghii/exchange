package com.exchange.oms.repository;

import com.exchange.oms.domain.OrderStatus;
import com.exchange.oms.domain.TradePair;
import org.springframework.data.jpa.repository.JpaRepository;

import com.exchange.oms.domain.Order;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByUserId(long userId);

    Optional<Order> findByIdAndStatus(Long orderId, OrderStatus orderStatus);

    Optional<Order> findByOrderIdAndTradePair(long orderId, TradePair pair);

}
