package com.iiaccount.utils;

import com.iiaccount.functioins.Function;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FunctionUtil {

    //所有继承Function的类
    private static final Map<String, Class<? extends Function>> functionsMap = new HashMap<String, Class<? extends Function>>();
    static {
        List<Class<?>> clazzes = ClassFinder.getAllAssignedClass(Function.class);
        clazzes.forEach((clazz) -> {
            try {
                // function
                Function tempFunc = (Function) clazz.newInstance();
                String referenceKey = tempFunc.getReferenceKey();

                if (referenceKey.length() > 0) { // ignore self
                    functionsMap.put(referenceKey, tempFunc.getClass());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public static boolean isFunction(String functionName){
        return functionsMap.containsKey(functionName);
    }

    public static String getValue(String functionName,String[] args){
        try {
            return functionsMap.get(functionName).newInstance().excute(args);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

}
