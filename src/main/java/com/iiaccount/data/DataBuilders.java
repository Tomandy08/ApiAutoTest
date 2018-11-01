package com.iiaccount.data;

import com.iiaccount.asserts.RespondAssertForJson;
import com.iiaccount.common.RunCaseJson;
import com.iiaccount.dao.CRUDData;
import com.iiaccount.utils.AssembledMessForJson;
import io.restassured.response.Response;
import jxl.read.biff.BiffException;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//数据提供
public class DataBuilders {

    static Logger log = Logger.getLogger(DataBuilders.class);

    /*
     *目前提供以下四种供数方式:
     * -查询数据池供数：${dp.sql(select accountNo from M_account where status = 1)}
           表达式：(dp.sql)\((.*)\)
     * -查询数据库供数：${db.sql(select accountNo from M_account_card where status = 1)}
           表达式：(db.sql)\((.*)\)
     * -先接口请求，然后提取响应报文供数：${SendmsgYg.case023.post($.data.code)} 或${SendmsgYg.case023.get($.data.code)}
           表达式：(.*case.*)\((.*)\)
     * -先接口请求，然后查询数据库/池供数：${SendmsgYg.case023.post.db.sql(select accountNo from M_account_card where status = 1)}
           表达式：(.*case.*).db.sql\((.*)\)
           表达式：(.*case.*).dp.sql\((.*)\)  --暂时不用
     */
    protected static Pattern dataPoolPattern = Pattern.compile("(dp.sql)\\((.*)\\)");
    protected static Pattern databasePattern = Pattern.compile("(db.sql)\\((.*)\\)");
    protected static Pattern reponsePattern = Pattern.compile("(.*case.*).post\\((.*)\\)");
    protected static Pattern httpDataPoolPattern = Pattern.compile("(.*case.*).dp.sql\\((.*)\\)");
    protected static Pattern httpDdatabasePattern = Pattern.compile("(.*case.*).db.sql\\((.*)\\)");

    public static String dataprovide(String var) throws SQLException, IOException, ClassNotFoundException, BiffException {
        String value = "";
        Matcher dpMatch = dataPoolPattern.matcher(var);
        Matcher dbMatch = databasePattern.matcher(var);
        Matcher responseMatch = reponsePattern.matcher(var);
        Matcher httpDbMatch = httpDdatabasePattern.matcher(var);
        Matcher httpDpMatch = httpDataPoolPattern.matcher(var);


        if(dpMatch.find()){//查询数据池供数
            String sql = dbMatch.group(2);
            value = CRUDData.selectData(sql,"DP");
            log.info("查询数据池供数："+ value);
        }else if(dbMatch.find()){//查询数据库供数
            String sql = dbMatch.group(2);
            value = CRUDData.selectData(sql,"DB");
            log.info("查询数据库供数："+ value);
        }else if(responseMatch.find()){//先接口请求，然后提取响应报文供数
            String jsonPath = responseMatch.group(2); //$.data.code
            String response = runCase(responseMatch);

            //根据$.data.code获取响应报文中对应的值
            value = RespondAssertForJson.getBuildValue(response, jsonPath);

        }else if(httpDbMatch.find()){//先接口请求，然后查询数据库供数
            String sql = httpDbMatch.group(2); //select accountNo from M_account_card where status = 1
            runCase(httpDbMatch);

            //根据sql语句获取相应的值
            value = CRUDData.selectData(sql,"DB");

        }else if(httpDpMatch.find()){//先接口请求，然后查询数据池供数（由于接口请求后数据不入数据池，此场景可以用于只执行依赖接口）
            String sql = httpDpMatch.group(2); //select accountNo from M_account_card where status = 1
            runCase(httpDpMatch);

            //根据sql语句获取相应的值
            value = CRUDData.selectData(sql,"DP");
        }

        return value;
    }

    //根据caseNo进行接口请求
    public static String runCase(Matcher matcher) throws IOException, BiffException {

        String[] caseMess =  matcher.group(1).split(".");  //SendmsgYg.case023.post
        String fileName = caseMess[0]+".xls";  //案例文件名
        String caseNo = caseMess[1]; //案例编号
        String requestType = caseMess[2]; //请求类型post/get
        //String sql = matcher.group(2); //select accountNo from M_account_card where status = 1
        String bodyString = "";
        //该map只有一条记录
        for(String value1:AssembledMessForJson.assembleMess(fileName,caseNo).values()){
            bodyString = value1;
        }

        log.info("matcher.group(1)："+matcher.group(1));
        log.info("matcher.group(2)："+matcher.group(2));
        log.info("bodyString："+bodyString);

        //发送请求
        Response response = RunCaseJson.runCase(bodyString,requestType);

        return response.asString();
    }

}
