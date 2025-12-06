package com.exchange.me.domain;

public enum MatchEventStatus {
    SUBMITED("SUBMITED"),
    FILLED("FILLED"),
    CANCELED("CANCELED"),
    REJECTED("REJECTED");

    private String enumValue;

    MatchEventStatus(String enumValue){
        this.enumValue = enumValue;
    }

    @Override
    public String toString(){
        return enumValue;
    }
}
