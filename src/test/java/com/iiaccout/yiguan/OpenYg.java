package com.iiaccout.yiguan;

import com.iiaccount.asserts.RespondAssertForJson;
import com.iiaccount.common.RunCaseJson;
import com.iiaccount.common.SetUpTearDown;
import com.iiaccount.dao.CRUDData;
import com.iiaccount.data.DataProviders;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static com.iiaccount.asserts.Asserts.asserts;

/*
 *一贯开立II/III类户
 * 环境参数在SetUpTearDown 父类定义
 */
@Feature("分类账户改造")
public class OpenYg extends SetUpTearDown {

    @Story("分类账户开户")
    @Test(dataProvider = "dataprovider", dataProviderClass = DataProviders.class
            , description = "开户")
    public void runCase(String caseMess, String bodyString) throws IOException, SQLException, ClassNotFoundException {

        //发送请求
        Response response = RunCaseJson.runCase(bodyString, "post");

        //如果需要数据库断言，此处添加断言文件变量的map映射
        //可通过调用封装的方法取值，比如查数据库、提取响应报文、调用接口等方式。
        Map<String, String> map = new HashMap<>();
        //查询数据库获取，取不到值返回""
        String account = CRUDData.selectData("select accountNo from ACCOUNT where status =1","DB");
        //提取响应报文，取不到值返回NULL
        String custId = RespondAssertForJson.getBuildValue(response.asString(),"$.data.custid");
        //执行SendmsgYg接口的case023案例，然后提取响应报文的merchanId ，取不到值返回NULL
        String merchanId = RespondAssertForJson.getBuildValue("","${SendmsgYg.case023.post($.data.merchanId)}");

        map.put("ACCOUNT_NO", account);
        map.put("CUST_ID", custId);
        map.put("MERCHANT_ID", merchanId);

        //断言（包含响应报文断言和数据库断言）
        String xmlFileName = this.getClass().getSimpleName(); //数据库断言xml文件名（与类名保持一致）
        asserts(caseMess, bodyString, response.asString(), xmlFileName, map);
    }
}
