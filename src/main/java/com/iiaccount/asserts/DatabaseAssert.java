package com.iiaccount.asserts;

import com.iiaccount.dao.DBDPConnection;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

import static com.iiaccount.utils.GainSqlUtil.gainSqlFromMap;
import static com.iiaccount.utils.IsNumericUtil.isNumeric;

public class DatabaseAssert {

    static Logger log = Logger.getLogger(DatabaseAssert.class);

    private static Connection connection = null;

    /*
     * 读取临时xml文件，根据xml的检查点进行数据库检查，并返回检查结果
     */
    public static StringBuffer verifyDatabase(String caseNo, String filePath) throws IOException, ClassNotFoundException, SQLException {     //返回检查不通过的原因

        StringBuffer excuteResultStr = new StringBuffer();
        String flagResultStr = "";
        String sql = "";
        String table_name = "";
        Map<String, String> keyMap = new HashMap<String, String>();
        Map<String, String> columnMap = new HashMap<String, String>();

        // add by lrb 20180921 每检查一张表都需要链接一次数据库，性能损耗大，把connect放到外面
        connection = DBDPConnection.getDBConnection();

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new File(filePath));

            NodeList caseList = doc.getElementsByTagName("caseNo");

            for (int i = 0; i < caseList.getLength(); i++) {
                Node case_node = caseList.item(i); // 第一个caseNo节点

                Element elem0 = (Element) case_node; // caseNo节点对象
                String caseNo_name = elem0.getAttribute("case_no");
                log.info("案例编号:" + caseNo_name);

                if (caseNo_name.equals(caseNo)) {                // 根据caseNo入参选择性遍历
                    for (Node table_node = case_node.getFirstChild(); table_node != null; table_node = table_node
                            .getNextSibling()) {

                        if (table_node.getNodeType() == Node.ELEMENT_NODE) // 如果当前节点为元素节点
                        {

                            Element elem1 = (Element) table_node; // table节点对象
                            table_name = elem1.getAttribute("table_name"); // 获取表名
                            log.info("表名:" + table_name);
                        }

                        for (Node column_node = table_node.getFirstChild(); column_node != null; column_node = column_node
                                .getNextSibling()) {
                            if (column_node.getNodeType() == Node.ELEMENT_NODE) // 如果当前节点为元素节点
                            {
                                Element elem2 = (Element) column_node; // column节点对象
                                String priKey_name = elem2.getAttribute("key_name"); // 获取主键名
                                String column_name = elem2.getAttribute("column_name"); // 获取检查字段名

                                String value = "";
                                if (column_node.getFirstChild() != null) {  //可能会存在 <priKey key_name="ACCOUNT_NO"/>这类节点，导致取getNodeValue抛异常
                                    value = column_node.getFirstChild().getNodeValue(); //获取值
                                } else {
                                    value = "";
                                }

                                if (!priKey_name.equals("")) {
                                    keyMap.put(priKey_name, value);
                                    log.info("主键名:" + priKey_name + " 值：" + value);
                                }

                                if (!column_name.equals("")) {
                                    columnMap.put(column_name, value);
                                    log.info("列名:" + column_name + " 值：" + value);
                                }

                            }
                        }
                        if (keyMap.size() != 0 && columnMap.size() != 0) {
                            log.info("keyMap: " + keyMap);
                            log.info("columnMap: " + columnMap);

                            sql = gainSqlFromMap(table_name, keyMap);           //根据索引拼装sql
                            log.info("sql: " + sql);

                            flagResultStr = checkTable(sql, columnMap);                    //数据库检查

                            String strFlag[] = flagResultStr.split("&&&&%%%%%%@@@@");// 分割字符串得到数组

                            if (strFlag[0].equals("false")) {
                                log.info("检查结果：" + table_name + "检查不通过！");
                                excuteResultStr.append("【" + table_name + "表检查不通过】\n" + strFlag[1]);
                            } else {
                                excuteResultStr.append("【" + table_name + "表检查通过】\n" + strFlag[1]);
                            }
                            keyMap.clear();
                            columnMap.clear();
                        }
                    }
                }
            }

            // add by lrb 20180921 每检查一张表都需要链接一次数据库，性能损耗大，把connect放到外面
            if (connection != null)
                connection.close();

        } catch (Exception e) {
            e.printStackTrace();
            excuteResultStr.append("XML文件有误：" + e.getMessage());
            return excuteResultStr;
        }
        return excuteResultStr;       //返回检查结果
    }

    /*
     *
     */
    public static String checkTable(String sql, Map<String, String> map) throws ClassNotFoundException,
            IOException {

        StringBuffer resultStr = new StringBuffer();
        StringBuffer flagStr = new StringBuffer();

        Map<String, String> mapType = new HashMap<String, String>();

        // add by lrb 20180921 每检查一张表都需要链接一次数据库，性能损耗大，把connect放到外面
        //Connection connection = null;
        ResultSet result = null;
        boolean flag = true;

        try {
            // add by lrb 20180921 每检查一张表都需要链接一次数据库，性能损耗大，把connect放到外面
            //url = "jdbc:oracle:thin:@"+url;
            //Class.forName("oracle.jdbc.driver.OracleDriver");
            //connection = DriverManager.getConnection(url, user, password);// 地址，用户名，密码

            result = connection.prepareStatement(sql).executeQuery();

            ResultSetMetaData metadata = result.getMetaData(); // add by lrb
            // 20170927
            // 获取每一列的类型NUMBER,VERCHA2，DATE等
            for (int i = 1; i <= metadata.getColumnCount(); i++) { // add by lrb
                // 20170927
                mapType.put(metadata.getColumnName(i),
                        metadata.getColumnTypeName(i)); // add by lrb 20170927
            }

            log.info(mapType);

            if (result.next()) { // 只有一笔记录，无需while，只取第一笔记录

                for (String key : map.keySet()) {
                    flag = true; // 循环过后需初始化，防止下一次循环取了上一次的结果；

                    log.info(key
                            + "预期结果为：" + map.get(key) + ";实际结果为："
                            + result.getString(key));

                    if (result.getString(key) != null) //数据库获取的值不为null
                    {
                        if (!result.getString(key).trim().equals(map.get(key).trim()) && !map.get(key).toUpperCase().trim().equals("NOTNULL")) { // 预期结果和实际结果不匹配

                            flag = false;

                            /* map映射的值是字符串，对于金额11.80映射为11.80，但数据库查询返回的是11.8，导致比对是检查不通过；需做特殊处理；
                             * 如果列类型是NUMBER类型且为数字，则转换为Double类型再进行比较；
                             * add by lrb 20170927 mapType.get(key).equals("NUMBER")
                             */
                            if (mapType.get(key).equals("NUMBER")
                                    && isNumeric(result.getString(key).trim())
                                    && isNumeric(map.get(key).trim())) {// 如果为数值，再进行判断
                                if (Double.parseDouble(result.getString(key).trim()) == Double
                                        .parseDouble(map.get(key).trim())) // 字符串转换为数值再比较
                                    flag = true;
                            }

                            log.info("flag1:" + flag);

                        }

                        if (map.get(key).toUpperCase().trim().equals("NOTNULL")) {//只做非空校验
                            if (result.getString(key).trim().equals("") || result.getString(key) == null) {
                                flag = false;
                            }
                        }
                    } else {
                        if (!map.get(key).equals(""))   //如果数据库查回来的是null，但上送的预期结果不是空值，则当成校验不通过
                            flag = false;
                    }

                    log.info("最终的flag:" + flag);

                    // 对表的每个字段校验都输出预期和实际结果；//add by linrb 20171031
                    if (!map.get(key).toUpperCase().trim().equals("NOTNULL")) {
                        resultStr.append("<" + key + "字段>预期结果为：" + map.get(key)
                                + "，实际结果为：" + result.getString(key) + "\n");
                    } else {
                        resultStr.append("<" + key + "字段>只做非空校验" + "，实际结果为：" + result.getString(key) + "\n");
                    }


                    // if(flag == false) //有一个值检查不通过，则认定该表校验不过，退出循环 //add by
                    // linrb 20171031
                    // break; //add by linrb 20171031

                    // 此处需把每次循环的flag标志存起来，防止出现字段1检查不通过、字段2检查通过类型的场景，导致flag不准确；
                    flagStr.append(flag);

                }
                // 如果flagStr含有false,则表示有字段检查不通过，flag标志赋值为false
                if (flagStr.indexOf("false") != -1) {
                    flag = false;
                } else {
                    flag = true;
                }

                resultStr.append("\n");

            } else {
                log.info("找不到记录");
                resultStr.append("找不到记录！" + "\n"); // add by linrb 20171031
                flag = false;
            }
            if (result != null)
                result.close();
            // add by lrb 20180921 减少数据库请求次数，把connect放到外面
            //if (connection != null)
            //  connection.close();

        } catch (SQLException e) {
            flag = false; // 查询数据库抛异常，认为检查不通过；
            resultStr.append("比对数据库异常！" + "\n"); // add by linrb 20171031
            e.printStackTrace();
        }
        return flag + "&&&&%%%%%%@@@@" + resultStr; // 比对结果不通过则返回false
    }
}