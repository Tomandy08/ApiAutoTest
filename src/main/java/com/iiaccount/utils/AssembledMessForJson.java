package com.iiaccount.utils;

import com.google.gson.Gson;
import com.iiaccount.asserts.RespondAssertForJson;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/*
 *根据测试用例文件拼装报文body(json格式)
 * 输入一贯改造_短信发送.xls，return json串列表
 */
public class AssembledMessForJson {

    static Logger log = Logger.getLogger(AssembledMessForJson.class);
    static Map<String, Object> bodyMap = new HashMap<String, Object>();
    static Map<String, Object> dataMap = new HashMap<String, Object>();
    static Map<String, Object> caseMessMap = new HashMap<String, Object>();
    static Map<String, String> map = new HashMap<String, String>();

    /*
    @Test()
    public void test() throws IOException, BiffException, URISyntaxException {
        assembleMess("sendmsgYg.xls","case022");
    }
    */


    /*
     *入参为testCase目录下的案例数据文件名
     */
    public static Map<String, String> assembleMess(String fileName,String caseNo) throws IOException, BiffException {
        log.info("文件名：" + fileName);

        String filePath = new GetFileMess().getFilePath("testCase", fileName);
        File xlsFile = new File(filePath);
        Workbook workbook = Workbook.getWorkbook(xlsFile); // 获得工作簿对象
        Sheet sheet = workbook.getSheet(0); // 获得工作表
        int rows = sheet.getRows(); // 获得行数
        int cols = sheet.getColumns(); // 获得列数

        String pubArgs = new GetFileMess().getValue("pubArgsYg", "PublicArgs.properties");
        log.info("接口公共入参pubArgsYg：" + pubArgs);

        bodyMap.clear();
        dataMap.clear();
        caseMessMap.clear();

        for (int row = 1; row < rows; row++) {
            String yOn = sheet.getCell(3, row).getContents();
            String caseNo1 = sheet.getCell(0, row).getContents().toLowerCase();  //文件中的案例编号
            if (yOn.equals("Y") && caseNo.equals("")) {  //获取全部为Y的案例报文体
                getMap(sheet, cols, row, pubArgs);
            } else if (caseNo1.equals(caseNo.toLowerCase())) {  //获取单条案例报文体，不管执行标识是否为Y
                getMap(sheet, cols, row, pubArgs);
            }
        }

        workbook.close();
        return map;
    }

    public static void getMap(Sheet sheet, int cols, int row, String pubArgs){

        for (int col = 0; col < cols; col++) {

            String cellKey = sheet.getCell(col, 0).getContents();//表头
            String cellValue = sheet.getCell(col, row).getContents();//值
            if (col >= 5) {
                //appid,api,version属于公共入参,公共入参字段在PublicArgs.properties文件进行配置
                // getBuildValue(value1,value2)方法用于转换${}或者函数为对应的值
                if (pubArgs.toLowerCase().contains(cellKey.toLowerCase().trim())) {
                    bodyMap.put(cellKey, RespondAssertForJson.getBuildValue("", sheet.getCell(col, row).getContents()));
                } else {
                    dataMap.put(cellKey, RespondAssertForJson.getBuildValue("", sheet.getCell(col, row).getContents()));
                }
            } else {
                caseMessMap.put(cellKey, cellValue);
            }
        }
        bodyMap.put("data", dataMap);
        map.put(new Gson().toJson(caseMessMap), new Gson().toJson(bodyMap));
    }
}
