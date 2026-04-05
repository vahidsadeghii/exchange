package com.exchange.profile.service;

public interface MessagingService {

    AuthenticationEvent save(String topic, String eventData);
}
