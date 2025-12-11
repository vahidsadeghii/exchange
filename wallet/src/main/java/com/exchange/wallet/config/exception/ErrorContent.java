package com.exchange.wallet.config.exception;

public record ErrorContent(Integer errorCode, String userMessage, String timestamp,
                           String httpPath, int httpStatus, String errorMessage){
}