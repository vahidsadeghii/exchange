package com.exchange.profile.config.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Locale;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class RestErrorHandler {


    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorContent handleRuntimeException(RuntimeException ex, HttpServletRequest request) {
        String locale = resolveLocale(request);
        return parseException(ex, HttpStatus.INTERNAL_SERVER_ERROR, locale, request);
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorContent handleBadRequestException(BadRequestException e, HttpServletRequest request) {
        String locale = resolveLocale(request);
        return parseException(e, HttpStatus.BAD_REQUEST, locale, request);
    }

    @ExceptionHandler(UnAuthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorContent handleUnAuthorizedException(UnAuthorizedException e, HttpServletRequest request) {
        String locale = resolveLocale(request);
        return parseException(e, HttpStatus.UNAUTHORIZED, locale, request);
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorContent handleNotFoundException(NotFoundException e, HttpServletRequest request) {
        String locale = resolveLocale(request);
        return parseException(e, HttpStatus.NOT_FOUND, locale, request);
    }

    @ExceptionHandler(FallBackException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorContent handleFallBackException(FallBackException e, HttpServletRequest request) {
        String locale = resolveLocale(request);
        return parseException(e, HttpStatus.INTERNAL_SERVER_ERROR, locale, request);
    }

    private String resolveLocale(HttpServletRequest request) {
        String locale = request.getHeader("locale");
        return StringUtils.hasText(locale) ? locale : "en";
    }

    private ErrorContent parseException(
            Throwable e,
            HttpStatus status,
            String locale,
            HttpServletRequest request
    ) {
        e.printStackTrace();

        String errorMessage;
        if (e instanceof BusinessException) {
            errorMessage = MessageBundleLoader.getMessage(
                    e.getClass().getName(),
                    Locale.forLanguageTag(locale)
            );
        } else {
            errorMessage = MessageBundleLoader.getMessage(
                    "errorMessage",
                    Locale.forLanguageTag(locale)
            );
        }

        String[] messageItems = errorMessage.split("#");
        String path = request.getRequestURI();

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
