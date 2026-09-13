package com.exchange.me;

import com.exchange.me.domain.EngineSnapshot;
import com.exchange.me.domain.Order;
import com.exchange.me.sbe.*;
import com.exchange.me.service.EngineService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class EngineServiceSnapshotTest {


    @Test
    void shouldCreateAndRestoreSnapshot() {

        EngineService engineService = new EngineService();

        Order order1 = Order.builder()
                .id(1L)
                .timestamp(1000L)
                .userId(10L)
                .tradeSide(TradeSide.BUY)
                .orderType(OrderType.LIMIT)
                .tradePair(TradePair.BTC_USD)
                .marketType(MarketType.SPOT)
                .matchStatus(MatchStatus.SUBMITED)
                .quantity(100L)
                .price(50000L)
                .filled(20L)
                .expireDays(10L)
                .build();

        Order order2 = Order.builder()
                .id(2L)
                .timestamp(2000L)
                .userId(20L)
                .tradeSide(TradeSide.SELL)
                .orderType(OrderType.LIMIT)
                .tradePair(TradePair.BTC_USD)
                .marketType(MarketType.SPOT)
                .matchStatus(MatchStatus.SUBMITED)
                .quantity(50L)
                .price(51000L)
                .filled(0L)
                .expireDays(5L)
                .build();

        engineService.restoreOrders(List.of(order1, order2));

        EngineSnapshot snapshot = engineService.createSnapshot();

        assertNotNull(snapshot);
        assertEquals(2, snapshot.orders().size());

        assertTrue(snapshot.orders().stream().anyMatch(order -> order.getId() == 1L));
        assertTrue(snapshot.orders().stream().anyMatch(order -> order.getId() == 2L));
    }

    @Test
    void shouldRestoreEngineFromSnapshot() {

        EngineService originalEngine = new EngineService();

        Order order = Order.builder()
                .id(100L)
                .timestamp(12345L)
                .userId(999L)
                .tradeSide(TradeSide.BUY)
                .orderType(OrderType.LIMIT)
                .tradePair(TradePair.BTC_USD)
                .marketType(MarketType.SPOT)
                .matchStatus(MatchStatus.SUBMITED)
                .quantity(100L)
                .price(50000L)
                .filled(25L)
                .expireDays(7L)
                .build();

        originalEngine.restoreOrders(List.of(order));

        EngineSnapshot snapshot = originalEngine.createSnapshot();

        EngineService restoredEngine = new EngineService();

        restoredEngine.restoreOrders(snapshot.orders());

        Order restoredOrder = restoredEngine.getOrder(TradePair.BTC_USD, 100L);

        assertNotNull(restoredOrder);

        assertEquals(100L, restoredOrder.getId());
        assertEquals(999L, restoredOrder.getUserId());
        assertEquals(100L, restoredOrder.getQuantity());
        assertEquals(50000L, restoredOrder.getPrice());
        assertEquals(25L, restoredOrder.getFilled());

        assertEquals(75L, restoredOrder.getRemainingQuantity());
    }
}
