package com.exchange.profile.domain;

public enum UserAuthority {
    GET_PROFILE("GET_PROFILE"),
    SET_PROFILE("SET_PROFILE"),
    DELETE_PROFILE("DELETE_PROFILE"),
    UPDATE_PROFILE("UPDATE_PROFILE");

    private String enumValue;

    UserAuthority(String enumValue){
        this.enumValue= enumValue;
    }

    @Override
    public String toString(){
        return enumValue;
    }
}
