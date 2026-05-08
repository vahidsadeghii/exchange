package com.exchange.event.config;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;


@Component
public class CamelRoutes extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from("direct:start")
                .routeId("CamelRoutes")
                .threads().executorService("MyDefault")
                .to("log:done");
    }
}
