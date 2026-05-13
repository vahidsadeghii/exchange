package com.exchange.event.config.camel;



import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.Route;
import org.apache.camel.builder.RouteBuilder;


@RequiredArgsConstructor
@Slf4j
public class DynamicRouterBean implements Processor {

    @Override
    public void process(Exchange exchange) throws Exception {

        final String routeId = "DYNANMIC.ROUTE.1";
        Route route = exchange.getContext().getRoute(routeId);
        if (null == route) {
            log.info("No route exist, creating one with name ");
            exchange.getContext().addRoutes(new RouteBuilder() {
                public void configure() throws Exception {
                    from("direct:DYNANMIC.ROUTE.1").routeId(routeId).to("direct:myloggerRoute");
                }
            });
        } else {
            log.info("Route already exist, no action"+ route.getId());
        }
    }
}
