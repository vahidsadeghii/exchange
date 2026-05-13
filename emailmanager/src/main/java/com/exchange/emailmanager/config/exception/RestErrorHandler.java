package com.exchange.emailmanager.config.exception;


import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;

import java.util.Locale;



@RestControllerAdvice
@RequiredArgsConstructor
public class RestErrorHandler {
    private final MessageSource messageSource;


    @ExceptionHandler(value = RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public  ErrorContent handler(Throwable ex, ServerWebExchange exchange) {
        String locale = exchange.getRequest().getHeaders().getFirst("locale");
        if (StringUtils.isEmpty( locale ))
            locale = "en";
        return parsException(ex, locale);
    }
    @ExceptionHandler(value = BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public  ErrorContent handler(BadRequestException e, ServerWebExchange exchange) {
        String locale = exchange.getRequest().getHeaders().getFirst("locale");
        if (StringUtils.isEmpty( locale ))
            locale = "en";
        return parsException(e, locale);
    }
    @ExceptionHandler(value = UnAuthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public  ErrorContent handler(UnAuthorizedException e, ServerWebExchange exchange) {
        String locale = exchange.getRequest().getHeaders().getFirst("locale");
        if (StringUtils.isEmpty( locale ))
            locale = "en";
        return parsException(e, locale);
    }
    @ExceptionHandler(value = NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public  ErrorContent handler(NotFoundException e, ServerWebExchange exchange) {
        String locale = exchange.getRequest().getHeaders().getFirst("locale");
        if (StringUtils.isEmpty( locale ))
            locale = "en";
        return parsException(e, locale);
    }
//    @ExceptionHandler(FeignException.class)
//    @ResponseBody
//    public ResponseEntity<ErrorContent> handle(FeignException e) throws Exception {
//        return ResponseEntity.status(e.getStatus())
//                .contentType(MediaType.APPLICATION_JSON)
//                .body(new ObjectMapper().readValue(e.getMessage(), ErrorContent.class));
//    }

    @ExceptionHandler(FallBackException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ErrorContent handle(FallBackException e) {
        return parsException(e, null);
    }

    public ErrorContent parsException(Throwable e, String locale) {
        e.printStackTrace();

        String errorMessage = "";
        if (e instanceof BusinessException) {
            errorMessage = messageSource.getMessage(e.getClass().getName(),null,  Locale.forLanguageTag(locale));
        } else {
            errorMessage = messageSource.getMessage("errorMessage", null, Locale.forLanguageTag(locale));
        }
        String[] messageItems = errorMessage.split("#");

        return new ErrorContent(Integer.parseInt(messageItems[0]), messageItems[1]);
    }
}
