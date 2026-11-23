package com.exchange.coredispatcher.service;

import com.exchange.coredispatcher.client.coregateway.CoreGatewayClient;
import com.exchange.coredispatcher.client.coregateway.MatchInfoResponse;
import com.exchange.coredispatcher.dto.MatchInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchInfoService {
    private final CoreGatewayClient coreGatewayClient;

    @Scheduled(fixedDelayString = "${core-dispatcher.match-info.poll-interval-ms:3600000}")
    public void fetchMatchInfoPeriodically() {
        List<MatchInfo> matches = getMatchInfo(100);
        log.info("Retrieved {} match records from CoreGateway", matches.size());
    }

    public List<MatchInfo> getMatchInfo(int maxMatches) {
        List<MatchInfoResponse> matchInfos = coreGatewayClient.getMatchInfo(maxMatches);
        return matchInfos.stream().map(this::convertToMatchInfo).collect(Collectors.toList());
    }

    private MatchInfo convertToMatchInfo(MatchInfoResponse response) {
        return new MatchInfo(
                response.id(),
                response.executionId(),
                response.timestamp(),
                response.makerSide(),
                response.takerOrderId(),
                response.makerOrderId(),
                response.takerUserId(),
                response.makerUserId(),
                response.quantity(),
                response.price(),
                response.takerOriginalQuantity(),
                response.takerRemain(),
                response.makerOriginalQuantity(),
                response.makerRemain()
        );
    }
}
