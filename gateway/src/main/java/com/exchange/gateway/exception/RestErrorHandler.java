package com.exchange.gateway.exception;

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
    public ErrorContent handleRuntimeException(Throwable ex, ServerWebExchange exchange) {
        return handleException(ex, HttpStatus.INTERNAL_SERVER_ERROR, exchange);
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorContent handleBadRequestException(BadRequestException ex, ServerWebExchange exchange) {
        return handleException(ex, HttpStatus.BAD_REQUEST, exchange);
    }

    @ExceptionHandler(UnAuthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorContent handleUnAuthorizedException(UnAuthorizedException ex, ServerWebExchange exchange) {
        return handleException(ex, HttpStatus.UNAUTHORIZED, exchange);
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorContent handleNotFoundException(NotFoundException ex, ServerWebExchange exchange) {
        return handleException(ex, HttpStatus.NOT_FOUND, exchange);
    }

    @ExceptionHandler(FallBackException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorContent handleFallBackException(FallBackException ex, ServerWebExchange exchange) {
        return handleException(ex, HttpStatus.INTERNAL_SERVER_ERROR, exchange);
    }

    private ErrorContent handleException(Throwable e, HttpStatus status, ServerWebExchange exchange) {
        String locale = exchange.getRequest().getHeaders().getFirst("locale");
        if (!StringUtils.hasText(locale)) locale = "en";

        String requestUrl = exchange.getRequest().getURI().toString();
        return parsException(e, status, locale, requestUrl);
    }

    private ErrorContent parsException(Throwable e, HttpStatus status, String locale, String requestUrl) {
        e.printStackTrace();
        String errorMessage;
        if (e instanceof BusinessException) {
            errorMessage = MessageBundleLoader.getMessage(e.getClass().getName(), Locale.forLanguageTag(locale));
        } else {
            errorMessage = MessageBundleLoader.getMessage("errorMessage", Locale.forLanguageTag(locale));
        }

        String[] messageItems = errorMessage.split("#");

        log.info("Message: {} {} {} {} {} {}",
                Integer.parseInt(messageItems[0]),
                messageItems[1],
                LocalDateTime.now(),
                requestUrl,
                status.value(),
                status.name());

        return new ErrorContent(
                Integer.parseInt(messageItems[0]),
                messageItems[1],
                LocalDateTime.now().toString(),
                requestUrl,
                status.value(),
                status.name());
    }
}
