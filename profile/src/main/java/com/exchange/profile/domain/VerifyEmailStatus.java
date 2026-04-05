package com.exchange.profile.domain;

public enum VerifyEmailStatus {
    DISABLE("DISABLE"),
    ENABLE("ENABLE");

    private String enumValue;

    VerifyEmailStatus (String enumValue){
        this.enumValue = enumValue;
    }

    @Override
    public String toString(){
        return enumValue;
    }
}
