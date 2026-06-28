package com.exchange.me.repository;

import com.exchange.me.domain.Order;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;


@Repository
public class InMemoryOrderRepository {
      private final Map<Long, Order> orderMap = new ConcurrentHashMap<>();


    public Map<Long, Order> save(Order order) {
        orderMap.put(order.getId(), order);

        System.out.println(orderMap);

        return orderMap;
    }

    public Optional<Order> findById(Long orderId) {
        return Optional.ofNullable(orderMap.get(orderId));
    }

    public Order findByUserId(Long userId) {
        return orderMap.values().stream()
                .filter(order -> order.getUserId() == userId)
                .findFirst()
                .orElse(null);
    }


    public void delete(String orderId) {
        orderMap.remove(orderId);
    }

}
