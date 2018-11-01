package com.iiaccount.common;

import com.iiaccount.dao.ExcutSqlFile;
import com.iiaccount.utils.GetFileMess;
import io.restassured.RestAssured;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static com.iiaccount.utils.WritePropertiesUtil.writePropertiesFile;

/*
 *父类的注解可以被子类继承，所有的环境变量在父类进行配置
 *根据testng.xml文件传入的参数来选择环境参数，默认为yg_env
 */
public class SetUpTearDown {

    @BeforeSuite
    public void dataSetUp() throws SQLException, IOException, ClassNotFoundException {
        ExcutSqlFile.excute("setUpSQL"); //案例执行前参数维护
    }

    //环境配置
    @BeforeClass
    public void envSetUp() {
        try {
            String system = "env.properties";    //环境由filter配置
            RestAssured.baseURI = new GetFileMess().getValue("baseURI", system);
            RestAssured.basePath = new GetFileMess().getValue("basePath", system);
            RestAssured.port = Integer.parseInt(new GetFileMess().getValue("port", system));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     *创建environment.properties并放到allure-results目录下，测试报告展现
     */
    @AfterSuite
    public void createEnvPropertiesForReport() throws IOException {
        Map<String, String> data = new HashMap<>();
        String database = "iiaccount_db.properties";
        data.put("DatabaseLoginName", new GetFileMess().getValue("DB_Name", database));
        data.put("DatabaseLoginPass", new GetFileMess().getValue("DB_Password", database));
        data.put("DatabaseLoginIP", new GetFileMess().getValue("DB_IP", database));
        data.put("baseURI", RestAssured.baseURI + ":" + RestAssured.port + "/" + RestAssured.basePath);

        writePropertiesFile(data);
    }

    @AfterSuite
    public void dataTearDown() throws SQLException, IOException, ClassNotFoundException {
        //案例执行结束后，对数据池的数据进行清理（删除或更新状态）
        ExcutSqlFile.excute("tearDownSQL"); //案例执行后数据清理
    }

}
