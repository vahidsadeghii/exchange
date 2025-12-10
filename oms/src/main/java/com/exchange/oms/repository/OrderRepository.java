package com.exchange.oms.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.exchange.oms.domain.Order;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByUserId(long userId);

}
