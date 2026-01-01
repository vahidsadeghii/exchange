package com.exchange.profile.domain;

public enum UserType {
    USER("USER"),
    ADMIN("ADMIN");

    private String enumValue;

    UserType(String enumValue){
        this.enumValue= enumValue;
    }

    @Override
    public String toString(){
        return enumValue;
    }
}
