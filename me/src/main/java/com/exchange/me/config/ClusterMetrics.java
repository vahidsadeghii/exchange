package com.exchange.me.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;

public class ClusterMetrics {

    private final PrometheusMeterRegistry registry;

    public final Counter receivedMessages;
    public final Counter successfulResponses;
    public final Counter failedResponses;
    public final Counter backPressuredResponses;
    public final Counter adminActionResponses;

    public final Timer requestProcessingTimer;
    public final Timer responseOfferTimer;

    public ClusterMetrics(PrometheusMeterRegistry registry) {
        this.registry = registry;

        receivedMessages = Counter.builder("aeron.messages.received")
                .register(registry);

        successfulResponses = Counter.builder("aeron.responses.success")
                .register(registry);

        failedResponses = Counter.builder("aeron.responses.failed")
                .register(registry);

        backPressuredResponses =
                Counter.builder("aeron.responses.backpressured")
                        .register(registry);

        adminActionResponses =
                Counter.builder("aeron.responses.admin_action")
                        .register(registry);

        requestProcessingTimer = Timer.builder("aeron.request.processing")
                .description("Time spent handling an Aeron request")
                .publishPercentileHistogram()
                .register(registry);

        responseOfferTimer = Timer.builder("aeron.response.offer")
                .description("Time spent offering a response to an Aeron client session")
                .publishPercentileHistogram()
                .register(registry);
    }

    public MeterRegistry registry() {
        return registry;
    }
}
