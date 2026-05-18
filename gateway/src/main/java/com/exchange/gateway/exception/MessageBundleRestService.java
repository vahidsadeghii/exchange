package com.exchange.gateway.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;

@RestController
@RequestMapping(value = "/message")
public class MessageBundleRestService {

    @RequestMapping(value = "/{locale}/{msg}", method = RequestMethod.GET)
    public ResponseEntity<String> get(@PathVariable String locale, @PathVariable String msg) {
        String message = MessageBundleLoader.getMessage(msg, Locale.forLanguageTag(locale));
        return ResponseEntity.ok(message);
    }
}
