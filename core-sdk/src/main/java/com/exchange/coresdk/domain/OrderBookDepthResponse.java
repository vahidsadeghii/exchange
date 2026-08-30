package com.exchange.coresdk.domain;

import java.util.List;


public class OrderBookDepthResponse extends Response {

    private List<PriceLevelResponse> bids;

    private List<PriceLevelResponse> asks;

    public OrderBookDepthResponse(List<PriceLevelResponse> bids, List<PriceLevelResponse> asks){
        super(0);
        this.asks = asks;
        this.bids = bids;
    }
    public OrderBookDepthResponse(int errorCode) {
        super(errorCode);

    }


    public List<PriceLevelResponse> getBids() {
        return bids;
    }

    public void setBids(List<PriceLevelResponse> bids) {
        this.bids = bids;
    }

    public List<PriceLevelResponse> getAsks() {
        return asks;
    }

    public void setAsks(List<PriceLevelResponse> asks) {
        this.asks = asks;
    }
}
