package com.exchange.wallet.config.feign;


import com.exchange.wallet.config.exception.BusinessException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FeignException extends BusinessException {
    private int status;
    private String message;
}
