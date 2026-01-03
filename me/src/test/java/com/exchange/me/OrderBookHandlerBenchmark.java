package com.exchange.me;

import com.exchange.me.domain.Order;
import com.exchange.me.domain.OrderType;
import com.exchange.me.domain.TradePair;
import com.exchange.me.domain.TradeSide;
import com.exchange.me.handler.OrderBookHandler;
import org.junit.jupiter.api.Assertions;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.Deque;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 2)
@Measurement(iterations = 2)
@Fork(1)
@State(Scope.Thread)
public class OrderBookHandlerBenchmark {
    private OrderBookHandler orderBookHandler;
    private long bidsFirstPriceLevel;
    private long asksFirstPriceLevel;
    private Order topBidOrder;

    @Setup(Level.Iteration)
    public void setup() {
        orderBookHandler = new OrderBookHandler(
                TradePair.BTC_USD
        );

        Random random = new Random();
        // Initialize orderBookHandler with necessary data
        for (int i = 0; i < 1000_000; i++) {
            Order buyOrder = new Order(i, 0, i, TradeSide.BUY, OrderType.MARKET, TradePair.BTC_USD, random.nextInt(1000_000), random.nextDouble(1000_000), 0);
            Order sellOrder = new Order(1000_000 + i, 0, 1000_000 + 1, TradeSide.SELL, OrderType.MARKET, TradePair.BTC_USD, random.nextInt(1000_000), random.nextDouble(2000_000, 3000_000), 0);
            orderBookHandler.matchOrder(System.currentTimeMillis(), buyOrder);
            orderBookHandler.matchOrder(System.currentTimeMillis(), sellOrder);
        }

        OrderBookHandler.MarketDepth marketDepth = orderBookHandler.getMarketDepth(5);
        System.out.printf("marketDepth=%s\n", marketDepth);
        System.out.printf("bidsDepth=%d\n", orderBookHandler.getBids().size());
        System.out.printf("asksDepth=%d\n", orderBookHandler.getAsks().size());

        TreeMap<Long, Deque<Order>> bids = orderBookHandler.getBids();
        bidsFirstPriceLevel = bids.firstEntry().getKey();
        TreeMap<Long, Deque<Order>> asks = orderBookHandler.getAsks();
        asksFirstPriceLevel = asks.firstEntry().getKey();

        topBidOrder = bids.firstEntry().getValue().getFirst();
    }

    @Benchmark
    public void benchmarkMatchSellOrder() {
        Order testOrder = new Order(1, 0, 1, TradeSide.SELL, OrderType.MARKET, TradePair.BTC_USD, 1500, bidsFirstPriceLevel, 0);
        orderBookHandler.matchOrder(System.currentTimeMillis(), testOrder);
    }

    @Benchmark
    public void benchmarkMatchBuyOrder() {
        Order testOrder = new Order(1, 0, 1, TradeSide.BUY, OrderType.MARKET, TradePair.BTC_USD, 1500, asksFirstPriceLevel, 0);
        orderBookHandler.matchOrder(System.currentTimeMillis(), testOrder);
    }

    @Benchmark
    public void deleteAnOrder() {
        orderBookHandler.deleteOrder(1, topBidOrder);

    }
}
