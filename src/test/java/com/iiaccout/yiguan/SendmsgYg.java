package com.iiaccout.yiguan;

import com.iiaccount.common.RunCaseJson;
import com.iiaccount.common.SetUpTearDown;
import com.iiaccount.data.DataProviders;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.io.IOException;
import java.sql.SQLException;

import static com.iiaccount.asserts.Asserts.asserts;

/*
 *一贯短信发送接口
 * 环境参数在SetUpTearDown 父类定义
 */
@Feature("分类账户改造")
public class SendmsgYg extends SetUpTearDown {

    @Story("发送短信")
    @Test(dataProvider = "dataprovider", dataProviderClass = DataProviders.class,
            description = "发送短信")
    public void runCase(String caseMess, String bodyString) throws IOException, SQLException, ClassNotFoundException {

        //发送请求
        Response response = RunCaseJson.runCase(bodyString, "post");

        //只进行响应报文断言
        asserts(caseMess, bodyString, response.asString(), "", null);
    }
}
