package com.exchange.profile.domain;

public enum UserStatus {
    ACTIVE("ACTIVE"),
    BLOCK("BLOCK"),
    INACTIVE("INACTIVE");

    private String enumValue;

    UserStatus(String enumValue){
        this.enumValue= enumValue;
    }

    @Override
    public String toString(){
        return enumValue;
    }
}
