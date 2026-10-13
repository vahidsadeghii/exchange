package com.exchange.me.config;

import com.sun.net.httpserver.HttpServer;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class MetricsHttpServer implements AutoCloseable {

    private final HttpServer server;

    public MetricsHttpServer(PrometheusMeterRegistry registry, int port) throws IOException {

        server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);
        server.createContext("/metrics", exchange -> {
            byte[] body = registry.scrape().getBytes(StandardCharsets.UTF_8);

            exchange.getResponseHeaders().set("Content-Type", "text/plain; version=0.0.4; charset=utf-8");

            exchange.sendResponseHeaders(200, body.length);

            try (var output = exchange.getResponseBody()) {
                output.write(body);
            }
        });

        server.start();
    }

    @Override
    public void close() {
        server.stop(1);
    }
}
