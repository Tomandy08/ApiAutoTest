package com.iiaccount.common;

import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import ru.yandex.qatools.allure.annotations.Step;

import static io.restassured.RestAssured.given;

public class RunCaseJson {

    /*
     *post或get方式请求,返回响应报文（json格式）
     *@bodyString:json格式的请求报文体
     *@para:requestType post或get
     */
    public static Response runCase(String bodyString,String requestType){
        Response response = null;
        if(requestType.toLowerCase().equals("get"))
            response = given()
                    .contentType("application/json;charset=UTF-8")
                    .request()
                    .body(bodyString)
                    .get();
        else
            response = given()
                    .contentType("application/json;charset=UTF-8")
                    .request()
                    .body(bodyString)
                    .post();

        //打印格式化的参数
        //response.prettyPrint(); ////去掉部分日志 add by lrb 20181029

        return response;
    }
}
