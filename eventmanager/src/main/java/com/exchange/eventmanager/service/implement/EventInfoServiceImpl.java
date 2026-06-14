package com.exchange.eventmanager.service.implement;


import com.exchange.eventmanager.domain.EventInfo;
import com.exchange.eventmanager.repository.EventInfoRepository;
import com.exchange.eventmanager.service.EventInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;


@Service
@Transactional
@RequiredArgsConstructor
public class EventInfoServiceImpl implements EventInfoService {
    private final EventInfoRepository eventInfoRepository;


    @Override
    public void save(String tag, String title, String serviceName, String event, LocalDateTime date) {
        eventInfoRepository.save(EventInfo.builder()
                .tag(tag)
                .title(title)
                .serviceName(serviceName)
                .event(event)
                .createDate(LocalDateTime.now())
                .build());

    }

}
