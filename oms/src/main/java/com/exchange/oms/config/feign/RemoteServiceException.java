package com.exchange.oms.config.feign;


public class RemoteServiceException extends RuntimeException {

    private final int status;
    private final String body;
    private final String methodKey;

    public RemoteServiceException(int status, String body, String methodKey) {
        super("Remote service failed: " + status);
        this.status = status;
        this.body = body;
        this.methodKey = methodKey;
    }

    public int getStatus() {
        return status;
    }

    public String getBody() {
        return body;
    }

    public String getMethodKey() {
        return methodKey;
    }
}