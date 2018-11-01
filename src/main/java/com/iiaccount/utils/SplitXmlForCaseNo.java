package com.iiaccount.utils;

import com.iiaccount.asserts.ConstVariable;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Map;

/*
 *将databaseAssert目录下的数据库断言按caseNo拆分为临时xml文件
 */
public class SplitXmlForCaseNo {

    static Logger log = Logger.getLogger(SplitXmlForCaseNo.class) ;

    /*
    @Test
    public void test() throws IOException {
        String fileName = "OpenYg";
        String caseNo = "case085";
        Map<String,String> map = new HashMap<>();

        xmlOutTemp(fileName,caseNo,map);
    }
    */

    /*
     * 读取包含全部case的xml文件，区分caseNo生成临时的xml文件,并替换xml文件中的变量
     * @para fileName:文件名,约定跟类名保持一致
     * @para caseNo:案例编号
     * @para map:xml文件的变量映射
     */
    public static String xmlOutTemp(String fileName, String caseNo,
                                    Map<String, String> map) { // 区分case生成临时文件

        String table_name = "";
        String filePathIn = new GetFileMess().getFilePath("databaseAssert",fileName+".xml");
        String filePathOut = "";

        if(filePathIn != null){
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            try {
                DocumentBuilder db = dbf.newDocumentBuilder();

                Document doc = db.parse(new File(filePathIn)); // 读取xml文件

                NodeList caseList = doc.getElementsByTagName("caseNo");

                log.info("共有" + caseList.getLength() + "个caseNo节点");

                for (int i = 0; i < caseList.getLength(); i++) {
                    Node case_node = caseList.item(i); // 第一个caseNo节点

                    Element elem0 = (Element) case_node; // caseNo节点对象
                    String caseNo_name = elem0.getAttribute("case_no");

                    log.info("案例编号:" + caseNo_name);

                    if (caseNo_name.equals(caseNo)) { // 根据caseNo入参选择性遍历

                        // -----------------------------------------------------------------------------
                        // 操作的Document对象
                        Document document = db.newDocument();
                        // 设置XML的版本
                        document.setXmlVersion("1.0");
                        // 创建根节点
                        Element root = document.createElement("caseNo");
                        root.setAttribute("case_no", caseNo);
                        // 将根节点添加到Document对象中
                        document.appendChild(root);
                        // ------------------------------------------------------------------------------

                        for (Node table_node = case_node.getFirstChild(); table_node != null; table_node = table_node
                                .getNextSibling()) {

                            if (table_node.getNodeType() == Node.ELEMENT_NODE) // 如果当前节点为元素节点
                            {
                                Element elem1 = (Element) table_node; // table节点对象
                                table_name = elem1.getAttribute("table_name"); // 获取表名
                                log.info("表名:" + table_name);
                            }
                            // ------------------------------------------------------------------------------
                            Element tableElement = document.createElement("table");
                            // 设置page节点的name属性
                            tableElement.setAttribute("table_name", table_name);
                            root.appendChild(tableElement);
                            // ------------------------------------------------------------------------------

                            for (Node column_node = table_node.getFirstChild(); column_node != null; column_node = column_node
                                    .getNextSibling()) {
                                if (column_node.getNodeType() == Node.ELEMENT_NODE) // 如果当前节点为元素节点
                                {
                                    Element elem2 = (Element) column_node; // column节点对象
                                    String priKey_name = elem2
                                            .getAttribute("key_name"); // 获取主键名
                                    String column_name = elem2
                                            .getAttribute("column_name"); // 获取检查字段名

                                    String value = "";
                                    if(column_node.getFirstChild() != null){  //可能会存在 <priKey key_name="ACCOUNT_NO"/>这类节点，导致取getNodeValue抛异常
                                        value = column_node.getFirstChild().getNodeValue(); //获取值
                                    }else{
                                        value = "";
                                    }

                                    if (!priKey_name.equals("")) {

                                        log.info("主键名:" + priKey_name + " 值：" + value);
                                        // ------------------------------------------------------------------------------
                                        Element priKeyElement = document
                                                .createElement("priKey");
                                        // 设置page节点的name属性
                                        priKeyElement.setAttribute("key_name",
                                                priKey_name);
                                        priKeyElement.setTextContent(value);

                                        for (String keyName : map.keySet()) { // 遍历map，替换变量
                                            if (value.equals(keyName)) { // xml的变量存在map中,则使用map的值
                                                priKeyElement.setTextContent(map
                                                        .get(keyName));
                                                break;
                                            }
                                        }
                                        tableElement.appendChild(priKeyElement);
                                        // ------------------------------------------------------------------------------
                                    }

                                    if (!column_name.equals("")) {

                                        log.info("列名:" + column_name + " 值：" + value);
                                        // ------------------------------------------------------------------------------
                                        Element columnElement = document
                                                .createElement("column");
                                        // 设置page节点的name属性
                                        columnElement.setAttribute("column_name",
                                                column_name);
                                        columnElement.setTextContent(value);

                                        log.info("map:" + map);
                                        for (String keyName : map.keySet()) { // 遍历map，替换变量
                                            if (value.equals(keyName)) { // xml的变量存在map中,则使用map的值
                                                columnElement.setTextContent(map
                                                        .get(keyName));
                                                break;
                                            }
                                        }
                                        tableElement.appendChild(columnElement);
                                        // ------------------------------------------------------------------------------
                                    }
                                }
                            }
                            table_name = "";
                        }
                        // ------------------------------------------------------------------------------
                        // 开始把Document映射到文件
                        TransformerFactory transFactory = TransformerFactory
                                .newInstance();
                        Transformer transFormer = transFactory.newTransformer();
                        // 设置输出结果
                        DOMSource domSource = new DOMSource(document);
                        // 生成xml文件
                        File file = new File(ConstVariable.xmlOutputPath +"\\" +fileName + "_temp.xml");   //temp目录下

                        // 判断是否存在,如果不存在,则创建
                        if (!file.exists()) {
                            file.createNewFile();
                        }
                        // 文件输出流
                        FileOutputStream out = new FileOutputStream(file);
                        // 设置输入源
                        StreamResult xmlResult = new StreamResult(out);
                        // 输出xml文件
                        transFormer.transform(domSource, xmlResult);
                        // 测试文件输出的路径
                        log.info("数据库断言临时xml文件路径："+file.getAbsolutePath());

                        filePathOut = file.getAbsolutePath();
                        // ------------------------------------------------------------------------------
                    }
                }
            } catch (Exception e) {
                log.info(e.getMessage());
                e.printStackTrace();
                return "";
            }
        }

        return filePathOut;
    }
}
