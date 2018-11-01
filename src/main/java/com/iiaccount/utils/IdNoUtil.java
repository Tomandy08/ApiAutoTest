package com.iiaccount.utils;

import java.util.*;

/*
 *参考资料：https://blog.csdn.net/u012898245/article/details/79404745
 *参考资料：https://www.cnblogs.com/liuhongfeng/p/4789274.html?tvd
 *证件号组成规则
 * 户籍所在地（第1到第6位）+ 出生日期（第7到第14位）+ 落户派出所代码（第15、16位）+ 性别代码（第17位）+ 验证码（第18位）
 */
public class IdNoUtil {

    /**
     * 生成第18位身份证号
     *
     * @param
     * @return 身份证校验码的计算方法
     * 将前面的身份证号码17位数分别乘以不同的系数。从第一位到第十七位的系数分别为：7－9－10－5－8－4－2－1－6－3－7－9－10－5－8－4－2。
     * 将这17位数字和系数相乘的结果相加。
     * 用加出来和除以11，看余数是多少？
     * 余数只可能有0－1－2－3－4－5－6－7－8－9－10这11个数字。
     * 其分别对应的最后一位身份证的号码为1－0－X －9－8－7－6－5－4－3－2。
     */
    public static String verificationCode(String str17) {
        char[] chars = str17.toCharArray();
        if (chars.length < 17) {
            return " ";
        }
        int[] coefficient = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
        char[] resultChar = {'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};
        int[] numberArr = new int[17];
        int result = 0;
        for (int i = 0; i < numberArr.length; i++) {
            numberArr[i] = Integer.parseInt(chars[i] + "");
        }
        for (int i = 0; i < numberArr.length; i++) {
            result += coefficient[i] * numberArr[i];
        }
        return String.valueOf(resultChar[result % 11]);

    }

    /**
     * 随机获取落户派出所代码（第15、16位） + 性别代码（第17位）
     * 直接生成三位数
     *
     * @return
     */
    public static String randomCode() {
        int code = (int) (Math.random() * 1000);
        if (code < 10) {
            return "00" + code;
        } else if (code < 100) {
            return "0" + code;
        } else {
            return "" + code;
        }
    }

    /*
     * 随机生成出生日期
     * @return
     */
    public static String randomBirthday() {
        Calendar birthday = Calendar.getInstance();
        birthday.set(Calendar.YEAR, (int) (Math.random() * 60) + 1950);
        birthday.set(Calendar.MONTH, (int) (Math.random() * 12));
        birthday.set(Calendar.DATE, (int) (Math.random() * 31));
        StringBuilder builder = new StringBuilder();
        builder.append(birthday.get(Calendar.YEAR));
        long month = birthday.get(Calendar.MONTH) + 1;
        if (month < 10) {
            builder.append("0");
        }
        builder.append(month);
        long date = birthday.get(Calendar.DATE);
        if (date < 10) {
            builder.append("0");
        }
        builder.append(date);
        return builder.toString();
    }

    /**
     * 随机获取区号
     *
     * @param registerLocation
     * @return
     */
    public static String randomLocationCode(Map<String, Integer> registerLocation) {
        int index = (int) (Math.random() * registerLocation.size());
        Collection<Integer> values = registerLocation.values();
        Iterator<Integer> it = values.iterator();
        int i = 0;
        int locationCode = 0;
        while (i <= index && it.hasNext()) {
            i++;
            if (i == index) {
                locationCode = it.next();
            }
        }
        return String.valueOf(locationCode);
    }
}
