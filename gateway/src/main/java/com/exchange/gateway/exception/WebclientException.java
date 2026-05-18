package com.exchange.gateway.exception;




public class WebclientException extends RuntimeException {
    private int status;
    private String message;

    public WebclientException(String message,  int status) {
        this.status = status;
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
