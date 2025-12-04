package com.exchange.oms.domain;

public enum MatchEngineStatus {
    SUCCESS("SUCCESS"),
    FAILURE("FAILURE"),
    TIMEOUT("TIMEOUT"),
    WAITING("WAITING");

    private String enumValue;

    MatchEngineStatus(String enumValue){
        this.enumValue = enumValue;
    }

    @Override
    public String toString(){
        return enumValue;
    }
}
