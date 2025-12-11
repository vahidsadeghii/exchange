package com.exchange.wallet.config.exception;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;

import java.time.LocalDateTime;
import java.util.Locale;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class RestErrorHandler {

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorContent handleRuntimeException(RuntimeException ex, ServerWebExchange exchange) {
        String locale = exchange.getRequest().getHeaders().getFirst("locale");
        if (!StringUtils.hasText(locale)) {
            locale = "en";
        }
        return parseException(ex, HttpStatus.INTERNAL_SERVER_ERROR, locale, exchange);
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorContent handleBadRequestException(BadRequestException e, ServerWebExchange exchange) {
        String locale = exchange.getRequest().getHeaders().getFirst("locale");
        if (!StringUtils.hasText(locale)) {
            locale = "en";
        }
        return parseException(e, HttpStatus.BAD_REQUEST, locale, exchange);
    }

    @ExceptionHandler(UnAuthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorContent handleUnAuthorizedException(UnAuthorizedException e, ServerWebExchange exchange) {
        String locale = exchange.getRequest().getHeaders().getFirst("locale");
        if (!StringUtils.hasText(locale)) {
            locale = "en";
        }
        return parseException(e, HttpStatus.UNAUTHORIZED, locale, exchange);
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorContent handleNotFoundException(NotFoundException e, ServerWebExchange exchange) {
        String locale = exchange.getRequest().getHeaders().getFirst("locale");
        if (!StringUtils.hasText(locale)) {
            locale = "en";
        }
        return parseException(e, HttpStatus.NOT_FOUND, locale, exchange);
    }

    @ExceptionHandler(FallBackException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorContent handleFallBackException(FallBackException e, ServerWebExchange exchange) {
        String locale = exchange.getRequest().getHeaders().getFirst("locale");
        if (!StringUtils.hasText(locale)) {
            locale = "en";
        }
        return parseException(e, HttpStatus.INTERNAL_SERVER_ERROR, locale, exchange);
    }

    private ErrorContent parseException(Throwable e, HttpStatus status, String locale, ServerWebExchange exchange) {
        e.printStackTrace();

        String errorMessage;
        if (e instanceof BusinessException) {
            errorMessage = MessageBundleLoader.getMessage(e.getClass().getName(), Locale.forLanguageTag(locale));
        } else {
            errorMessage = MessageBundleLoader.getMessage("errorMessage", Locale.forLanguageTag(locale));
        }

        String[] messageItems = errorMessage.split("#");
        String path = exchange.getRequest().getURI().toString();

        log.info("Message: {}, {}, {}, {}, {}, {}",
                Integer.parseInt(messageItems[0]),
                messageItems[1],
                LocalDateTime.now(),
                path,
                status.value(),
                status.name()
        );

        return new ErrorContent(
                Integer.parseInt(messageItems[0]),
                messageItems[1],
                LocalDateTime.now().toString(),
                path,
                status.value(),
                status.name()
        );
    }
}
