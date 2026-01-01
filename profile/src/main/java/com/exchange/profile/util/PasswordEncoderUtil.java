package com.exchange.profile.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordEncoderUtil {

    private static BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();

    public static String encodePassword(String password) {
        return bcrypt.encode(password);
    }

    public static boolean isPasswordMatches(String password, String userPassword){
        return  bcrypt.matches(
                password,  userPassword);
    }
}
