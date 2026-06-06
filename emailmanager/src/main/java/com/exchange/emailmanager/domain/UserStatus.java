package com.exchange.emailmanager.domain;

public enum UserStatus {
    ACTIVE("ACTIVE"),
    INACTIVE("INACTIVE"),
    BLOCK("BLOCK");
    private String enumValue;
    UserStatus(String enumValue){
        this.enumValue = enumValue;
    }

    @Override
    public String toString(){
        return enumValue;
    }

}
