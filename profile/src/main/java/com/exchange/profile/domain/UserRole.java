package com.exchange.profile.domain;

public enum UserRole {
    ROLE_USER("ROLE_USER"),
    ROLE_CUSTOMER("ROLE_CUSTOMER");

    private String enumValue;
    UserRole(String enumValue){
        this.enumValue = enumValue;
    }

    @Override
    public String toString(){
        return enumValue;
    }
}
