package com.exchange.me;

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
import org.openjdk.jmh.infra.Blackhole;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 3)
@Measurement(iterations = 5)
@Fork(2)
public class SteadyStateThroughputBenchmark {
    static final int WORKERS = Runtime.getRuntime().availableProcessors();

    volatile boolean running;
    LongAdder completed;

    @Setup(Level.Iteration)
    public void setup() {
        running = true;
        completed = new LongAdder();
    }

    @Benchmark
    public long platformThreads() throws Exception {
        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < WORKERS; i++) {
            Thread t = new Thread(() -> {
                while (running) {
                    Blackhole.consumeCPU(100);
                    completed.increment();
                }
            });
            threads.add(t);
            t.start();
        }

        Thread.sleep(1000); // measurement window
        running = false;

        for (Thread t : threads) {
            t.join();
        }

        return completed.sum();
    }

    @Benchmark
    public long virtualThreads() throws Exception {
        completed.reset();
        running = true;

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < WORKERS; i++) {
                executor.submit(() -> {
                    while (running) {
                        Blackhole.consumeCPU(100);
                        completed.increment();
                    }
                });
            }

            Thread.sleep(1000);
            running = false;
        }

        return completed.sum();
    }
}
