package com.exchange.me;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 5)
@Measurement(iterations = 10)
@Fork(2)
@State(Scope.Benchmark)
public class ThreadComparingBenchmark {
    private ExecutorService pThreadExecutor;
    private ExecutorService vThreadExecutor;

    @Setup
    public void setup() {
        pThreadExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        vThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();
    }

    @TearDown
    public void tearDown() {
        pThreadExecutor.shutdownNow();
        vThreadExecutor.shutdownNow();
    }

    @Benchmark
    public void platformThreads() {
        try {
            pThreadExecutor.submit(() -> {
                Blackhole.consumeCPU(10);
            }).get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Benchmark
    public void virtualThreads() {
        try {
            vThreadExecutor.submit(() -> {
                Blackhole.consumeCPU(10);
            }).get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Benchmark
    public void platformBlocking() throws Exception {
        pThreadExecutor.submit(() -> {
            try {
                Thread.sleep(10);
                return null;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Benchmark
    public void virtualBlocking() throws Exception {
        vThreadExecutor.submit(() -> {
            try {
                Thread.sleep(10);
                return null;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

}
