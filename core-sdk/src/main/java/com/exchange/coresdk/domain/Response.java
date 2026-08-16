package com.exchange.coresdk.domain;

public class Response{
    private final int errorCode;

    public  Response(int errorCode){
        this.errorCode = errorCode;
    }

    public int getErrorCode(){
        return errorCode;
    }
}
