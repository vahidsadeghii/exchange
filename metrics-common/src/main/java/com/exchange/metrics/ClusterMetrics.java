package com.exchange.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

public class ClusterMetrics {

    public final Counter receivedMessages;
    public final Counter successfulResponses;
    public final Counter failedResponses;
    public final Counter backPressuredResponses;
    public final Counter adminActionResponses;
    public final Timer requestProcessingTimer;
    public final Timer responseOfferTimer;

    public ClusterMetrics(MeterRegistry registry) {
        receivedMessages = Counter.builder("aeron.messages.received")
                .register(registry);
        successfulResponses = Counter.builder("aeron.responses.success")
                .register(registry);
        failedResponses = Counter.builder("aeron.responses.failed")
                .register(registry);
        backPressuredResponses = Counter.builder("aeron.responses.backpressured")
                .register(registry);
        adminActionResponses = Counter.builder("aeron.responses.admin_action")
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
}
