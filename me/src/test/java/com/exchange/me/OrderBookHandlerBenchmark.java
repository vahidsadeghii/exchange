package com.exchange.me;

import com.exchange.me.sbe.MarketType;
import com.exchange.me.sbe.MatchStatus;
import com.exchange.me.sbe.OrderType;
import com.exchange.me.sbe.TradePair;
import com.exchange.me.sbe.TradeSide;
import com.exchange.me.domain.Order;
import com.exchange.me.handler.OrderBookHandler;
import com.exchange.me.handler.OrderHandlerFactory;
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
        orderBookHandler = new OrderBookHandler(TradePair.BTC_USD, OrderHandlerFactory.createFactory());

        Random random = new Random();
        // Initialize orderBookHandler with necessary data
        for (long i = 0; i < 1000_000; i++) {
            Order buyOrder =
                    new Order(
                            i,
                            0l,
                            i,
                            TradeSide.BUY,
                            OrderType.MARKET,
                            TradePair.BTC_USD,
                            MarketType.SPOT,
                            MatchStatus.SUBMITED,
                            random.nextLong(1000_000),
                            random.nextLong(1000_000),
                            0,
                            0);
            Order sellOrder =
                    new Order(
                            1000_000 + i,
                            0,
                            1000_000 + 1,
                            TradeSide.SELL,
                            OrderType.MARKET,
                            TradePair.BTC_USD,
                            MarketType.SPOT,
                            MatchStatus.SUBMITED,
                            random.nextLong(1000_000),
                            random.nextLong(2000_000, 3000_000),
                            0,
                            0);
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
        Order testOrder =
                new Order(
                        1l,
                        0l,
                        1l,
                        TradeSide.SELL,
                        OrderType.MARKET,
                        TradePair.BTC_USD,
                        MarketType.SPOT,
                        MatchStatus.SUBMITED,
                        1500l,
                        bidsFirstPriceLevel,
                        0l,
                        0L);
        orderBookHandler.matchOrder(System.currentTimeMillis(), testOrder);
    }

    @Benchmark
    public void benchmarkMatchBuyOrder() {
        Order testOrder =
                new Order(
                        1,
                        0,
                        1,
                        TradeSide.BUY,
                        OrderType.MARKET,
                        TradePair.BTC_USD,
                        MarketType.SPOT,
                        MatchStatus.SUBMITED,
                        1500l,
                        asksFirstPriceLevel,
                        0,
                        0);
        orderBookHandler.matchOrder(System.currentTimeMillis(), testOrder);
    }

    @Benchmark
    public void deleteAnOrder() {
        orderBookHandler.deleteOrder(1, topBidOrder);
    }
}
