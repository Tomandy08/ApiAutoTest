package com.iiaccount.utils;

import java.util.Map;
import java.util.Set;

public class GainSqlUtil {

    /*
     *拼装sql语句
     */
    public static String gainSqlFromMap(String tableName,
                                        Map<String, String> map) {
        String sql = "select * from " + tableName + " where 1=1 ";
        Set<String> set = map.keySet();

        for (String keyName : set) {
            String value = map.get(keyName);
            if (value != null) {
                sql = sql + " and " + keyName + " = " + "'" + value + "'";
            }
        }
        return sql;
    }
}
