package com.iiaccount.utils;

import java.util.Random;

public class RandomUtil {

    public static String randomBase = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";//字母和数字
    public static String randomNumberBase = "0123456789";

    public static Random random = new Random();

    public static String getRandom(int length, boolean onlyNumber) {
        String base;
        if (onlyNumber) {
            base = randomNumberBase;
        } else {
            base = randomBase;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            char chr;
            do {
                int number = random.nextInt(base.length());
                chr = base.charAt(number);
            } while (i==0&&chr=='0') ;//第一个字符不能为0,

            sb.append(chr);
        }
        return sb.toString();
    }
}
