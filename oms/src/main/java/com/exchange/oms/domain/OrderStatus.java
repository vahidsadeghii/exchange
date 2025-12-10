package com.exchange.oms.domain;


public enum OrderStatus {
    NEW("NEW"),
    PARIAL_FILL("PARIAL_FILL"),
    FILLED("FILLED"),
    CANCELED("  CANCELED"),
    REJECTED("REJECTED");

    private String enumValue;

    OrderStatus(String enumValue) {
        this.enumValue = enumValue;
    }

    @Override
    public String toString() {
        return enumValue;
    }
}
