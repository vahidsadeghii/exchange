package com.exchange.profile.domain;

public enum GenderType {
    MALE("MALE"),
    FEMALE("FEMALE"),
    TRANSGENDER("TRANSGENDER");

    private String enumValue;
    GenderType(String enumValue){
        this.enumValue = enumValue;
    }

    @Override
    public String toString(){
        return enumValue;
    }
}
