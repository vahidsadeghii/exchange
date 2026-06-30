package com.exchange.oms.domain;

public enum MarketType {
    MARKET("MARKET"),
    FOK("FOK");

     private String enumValue;
    MarketType(String enumValue){
        this.enumValue= enumValue;
    }

    @Override
    public String toString(){
        return enumValue;
    }

}
