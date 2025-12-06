package com.exchange.me.domain;

public enum MatchEngineEventStatus {
    SUBMITED("SUBMITED"),
    FILLED("FILLED"),
    CANCELED("CANCELED"),
    REJECTED("REJECTED");

    private String enumValue;

    MatchEngineEventStatus(String enumValue){
        this.enumValue = enumValue;
    }

    @Override
    public String toString(){
        return enumValue;
    }
}
