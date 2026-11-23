package com.exchange.coredispatcher.config.exception;

public record ErrorContent(Integer errorCode, String userMessage, String timestamp,
                           String httpPath, int httpStatus, String errorMessage){
}