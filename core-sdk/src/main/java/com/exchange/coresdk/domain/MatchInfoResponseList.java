package com.exchange.coresdk.domain;

import java.util.List;

public class MatchInfoResponseList extends Response {
    private final List<MatchInfoResponse> matches;

    public MatchInfoResponseList(int errorCode) {
        super(errorCode);
        this.matches = List.of();
    }

    public MatchInfoResponseList(List<MatchInfoResponse> matches) {
        super(0);
        this.matches = List.copyOf(matches);
    }

    public List<MatchInfoResponse> getMatches() {
        return matches;
    }
}
