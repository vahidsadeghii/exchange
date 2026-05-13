package com.exchange.profile.util;

import java.util.Random;

public class RandomUtil {

    public static int generateRandomNumber(int min, int max){
        Random random = new Random();
        return min + random.nextInt(max);
    }
}
