package com.iiaccount.data;

import com.iiaccount.utils.AssembledMessForJson;
import jxl.read.biff.BiffException;
import org.testng.annotations.DataProvider;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class DataProviders {

    /*
     *map包含两部分json，key为caseNo等信息，value为接口入参
     */
    @DataProvider(name = "dataprovider",parallel = true)
    public static Object[][] dataP(Method method) throws IOException, BiffException, URISyntaxException {
        String className = method.getDeclaringClass().getSimpleName(); //获取类名
        String caseFileName = className+".xls"; //测试案例名称为：类名.xls

        Object[][] objects = null;
        Map<String,String> map = new HashMap<String, String>();
        map = AssembledMessForJson.assembleMess(caseFileName,""); //""表示读取所有的为Y的case
        objects = new Object[map.size()][2];
        int i=0;
        for(Map.Entry<String, String> entry : map.entrySet()){
            objects[i][0] = entry.getKey();
            objects[i][1] = entry.getValue();
            i++;
        }
        map.clear();  //需清空map，否则案例会不断叠加 2018-10-19 add by lrb
        return objects;
    }
}
