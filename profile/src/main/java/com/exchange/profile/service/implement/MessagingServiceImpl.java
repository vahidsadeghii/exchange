package com.exchange.profile.service.implement;

import com.exchange.profile.service.MessagingService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class MessagingServiceImpl implements MessagingService {

    @Override
    public AuthenticationEvent save(String topic, String eventData) {
        return null;
    }
}
