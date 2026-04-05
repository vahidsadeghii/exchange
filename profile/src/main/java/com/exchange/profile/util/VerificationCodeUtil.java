package com.exchange.profile.util;

public class VerificationCodeUtil {

    public static String getVerificationCode(){
        return String.valueOf(RandomUtil.generateRandomNumber(100000, 899999));
    }
}
