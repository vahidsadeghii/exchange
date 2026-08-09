package com.exchange.me.service.impl;

import com.exchange.me.domain.MatchInfo;
import com.exchange.me.repository.MatchInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MatchPersistenceService {

    private final MatchInfoRepository repository;

    public void saveAll(List<MatchInfo> matches) {
        if (matches == null || matches.isEmpty()) {
            return;
        }

        repository.saveAll(matches);
    }
}
