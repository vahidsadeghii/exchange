package com.exchange.oms.config.feign;

import feign.Response;
import feign.codec.ErrorDecoder;
import feign.Util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class FeignErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {

        String body = "";

        try {
            if (response.body() != null) {
                body = Util.toString(
                        response.body().asReader(StandardCharsets.UTF_8)
                );
            }
        } catch (IOException e) {
            body = "UNREADABLE_RESPONSE_BODY";
        }

        return new RemoteServiceException(
                response.status(),
                body,
                methodKey
        );
    }
}