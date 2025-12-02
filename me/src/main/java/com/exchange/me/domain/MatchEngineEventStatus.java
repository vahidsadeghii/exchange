package com.exchange.me.domain;

public enum MatchEngineEventStatus {
    SUCCESS("SUCCESS"),
    FAILURE("FAILURE"),
    TIMEOUT("TIMEOUT"),
    WAITING("WAITING");

    private String enumValue;

    MatchEngineEventStatus(String enumValue){
        this.enumValue = enumValue;
    }

    @Override
    public String toString(){
        return enumValue;
    }
}
