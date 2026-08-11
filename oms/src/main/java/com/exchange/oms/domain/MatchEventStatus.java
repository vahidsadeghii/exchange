package com.exchange.oms.domain;

public enum MatchEventStatus {
    FILLED("FILLED"),
    PARTIALLY_FILLED(" PARTIALLY_FILLED"),
    CANCELED("CANCELED"),
    REJECTED("REJECTED");

    private String enumValue;

    MatchEventStatus(String enumValue) {
        this.enumValue = enumValue;
    }

    @Override
    public String toString() {
        return enumValue;
    }
}
