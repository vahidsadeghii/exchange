package com.exchange.event.service.implement;


import com.exchange.event.domain.EventInfo;
import com.exchange.event.repository.EventInfoRepository;
import com.exchange.event.service.EventInfoService;
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

        eventInfoRepository.save(
                EventInfo.builder()
                        .tag(tag)
                        .destinationTopic(title)
                        .serviceName(serviceName)
                        .event(event)
                        .createDate(
                                date != null
                                        ? date
                                        : LocalDateTime.now()
                        )
                        .build()
        );
    }
}

