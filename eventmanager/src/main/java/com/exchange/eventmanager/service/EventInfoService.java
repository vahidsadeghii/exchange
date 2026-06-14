package com.exchange.eventmanager.service;


import java.time.LocalDateTime;

public interface EventInfoService {
    void save(String tag, String titles, String serviceName, String event, LocalDateTime date);

}
