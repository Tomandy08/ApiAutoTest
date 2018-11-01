package com.iiaccount.asserts;

import com.alibaba.fastjson.JSONPath;
import com.iiaccount.data.DataBuilders;
import com.iiaccount.utils.FunctionUtil;
import com.iiaccount.utils.StringUtil;
import org.apache.log4j.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RespondAssertForJson {

    static Logger log = Logger.getLogger(RespondAssertForJson.class);

    /**
     * 替换符，如果数据中包含“${}”则会被替换成公共参数中存储的数据
     */
    protected static Pattern replaceParamPattern = Pattern.compile("\\$\\{(.*?)\\}");

    /**
     * 截取自定义函数正则表达式：__random(value1,value2)
     */
    protected static Pattern funPattern = Pattern
            .compile("__(\\w*?)\\((([\\w]*,?)*)\\)");

    /*
     *包含断言的正则表达式,__contain(sssss)
     */
    protected static Pattern containPattern = Pattern.compile("__(contain)\\(.+\\)");

    /*
     *对预期结果断言，json断言和contain断言允许在同一预期结果中使用，比如$.status=200;__contain(tomandy)
     *无论断言成功还是失败，测试报告展现断言结果
     */
    public static StringBuffer verifyResult(String sourceData, String verifyData) {
        if (verifyData.equals("") || verifyData == null)
            return null;

        log.info("待验证的预期结果为：" + verifyData);

        boolean assertFlag = true;
        StringBuffer stringBuffer = new StringBuffer();

        String assertStr[] = verifyData.split(";");
        for (String assertString : assertStr) {

            if (assertString.toLowerCase().contains("__contain(")) {
                // 验证包含断言
                log.info("contain断言表达式：" + assertString);
                //提取__contain()函数里的字符串
                String containMess = assertString.substring(10, assertString.length() - 1);
                assertFlag = ContainAssert.contains(sourceData, containMess);
                if (!assertFlag)
                    stringBuffer.append("【" + assertString + "断言" + assertFlag + String.format("，期待\n'%s'\n包含'%s'，实际不包含！】\n", sourceData, containMess));
                else
                    stringBuffer.append("【" + assertString + "断言" + assertFlag + String.format("，期待\n'%s'\n包含'%s'，实际包含！】\n", sourceData, containMess));
            } else if (assertString.toLowerCase().contains("$.")) {
                log.info("json断言表达式：" + assertString);
                //json断言，通过;隔开
                Pattern pattern = Pattern.compile("([^;]*)=([^;]*)");
                Matcher matcher = pattern.matcher(assertString.trim());
                while (matcher.find()) {
                    //根据$.status的json格式匹配响应报文中的值
                    String actualValue = getBuildValue(sourceData, matcher.group(1));
                    log.info("matcher.group(1):" + matcher.group(1));

                    //假如预期结果为$.status=200，则匹配值200，200也可以替换为自定义函数
                    String exceptValue = getBuildValue(sourceData, matcher.group(2));
                    log.info("matcher.group(2):" + matcher.group(2));

                    log.info(String.format("验证转换后的值%s=%s", actualValue,
                            exceptValue));

                    //如果有多个断言，前面断言的失败，不会再校验后面的断言
                    //Assert.assertEquals(actualValue, exceptValue, "验证预期结果失败！");
                    //无论断言成功还是失败，都保存断言
                    if (exceptValue.equals(actualValue)) {
                        assertFlag = true;
                        stringBuffer.append("【" + matcher.group() + "断言" + assertFlag + String.format("，期待预期结果为'%s'，实际结果为'%s'！】\n", exceptValue, actualValue));
                    } else {
                        assertFlag = false;
                        stringBuffer.append("【" + matcher.group() + "断言" + assertFlag + String.format("，期待预期结果为'%s'，实际结果为'%s'！】\n", exceptValue, actualValue));
                    }
                }
            } else {
                //Assert.assertTrue(false, "【预期结果断言格式有误,目前仅支持Json及contain断言，多个断言使用英文分号隔开，例如：$.status=200;__contain(tomandy)】");
                assertFlag = false;
                stringBuffer.append("【预期结果断言" + assertFlag + "，断言格式有误,目前仅支持Json及contain断言，多个断言使用英文分号隔开，例如：$.status=200;__contain(tomandy)】\n");
            }
        }
        return stringBuffer;
    }

    /**
     * 支持json串转换
     * 支持自定义函数的转换
     * 支持${}变量转换
     *
     * @param sourchJson
     * @param key
     * @return
     */
    public static String getBuildValue(String sourchJson, String key) {
        key = key.trim();
        Matcher funMatch = funPattern.matcher(key);
        Matcher replacePattern = replaceParamPattern.matcher(key);

        //log.info("key is:" + key); //去掉部分日志 add by lrb 20181029
        try{
            if (key.startsWith("$.")) {// jsonpath
                key = JSONPath.read(sourchJson, key).toString();  //jsonpath读取对应的值
                log.info("key start with $.,value is:" + key);
            } else if (funMatch.find()) {//函数

                String args = funMatch.group(2);  //函数入参
                log.info("key is a function,args is:" + args);
                String[] argArr = args.split(",");
                for (int index = 0; index < argArr.length; index++) {
                    String arg = argArr[index];
                    if (arg.startsWith("$.")) {  //函数入参亦支持json格式
                        argArr[index] = JSONPath.read(sourchJson, arg).toString();
                    }
                }
                log.info("argArr："+argArr.length);
                String value = FunctionUtil.getValue(funMatch.group(1), argArr);  //函数名不区分大小写，返回函数值
                log.info("函数名 funMatch.group(1):" + funMatch.group(1));
                key = StringUtil.replaceFirst(key, funMatch.group(), value);  //把函数替换为生成的值
                log.info("函数 funMatch.group():" + funMatch.group());
                log.info("key is a function,value is:" + key);
            } else if (replacePattern.find()) {//${}变量
                log.info("${}变量体："+replacePattern.group(1));
                String var = replacePattern.group(1).trim();

                String value1 = DataBuilders.dataprovide(var);
                key = StringUtil.replaceFirst(key, replacePattern.group(), value1);  //把变量替换为生成的值
                log.info("key is a ${} pattern,value is:" + key);
            }
            return key;

        }catch(Exception e){

            log.info(e.getMessage());
            return  null;
        }
    }

}
