package com.exchange.me.domain;

import org.openjdk.jmh.annotations.Fork;

public enum OrderType {
    LIMIT("LINIT"),
    MARKET("MARKET"),
    Fork("FOK");

    private String enumValue;

    OrderType(String enumValue) {
        this.enumValue = enumValue;
    }

    @Override
    public String toString() {
        return enumValue;
    }
}
