package com.exchange.me.domain;

public enum TradePair {
    BTC_USD("BTC_USD"),
    ETH_USD("ETH_USD"),
    LTC_USD("LTC_USD"),
    XRP_USD("XRP_USD"),
    TTR_USD("TTR_USD"),
    BTC_EURO("BTC_EURO"),
    ETH_EURO("ETH_EURO"),
    LTC_EURO("LTC_EURO"),
    XRP_EURO("XRP_EURO"),
    TTR_EURO("TTR_EURO");

    private String enumValue;
    TradePair(String enumValue){
        this.enumValue= enumValue;
    }

    @Override
    public String toString(){
        return enumValue;
    }
}
