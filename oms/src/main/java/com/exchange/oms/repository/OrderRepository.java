package com.exchange.oms.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.exchange.oms.domain.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {

}
