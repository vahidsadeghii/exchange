package com.exchange.event.service.implement;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AsyncRefreshService {

    @Async
    public void refreshAsync(TagRouterServiceImpl routerService) {
        routerService.refreshRout();
    }
}

