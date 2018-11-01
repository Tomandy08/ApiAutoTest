# ApiAutoTest
框架介绍详见文章：[基于TestNG+Rest Assured+Allure的接口自动化测试框架](https://www.jianshu.com/p/880f5eeba016)
# 约定规则
- 数据库使用的是oracle，数据池使用mysql
- 部署时根据需要配置日志输出路径，log4j.properties
- 案例执行前依赖参数在sql/setUpSQL.sql维护
- 案例执行后清理数据在sql/tearDownSQL.sql维护
- resources/testCase及databaseAssert目录下的文件名与test测试类类名保持一致
- 接口公共入参字段在resources/PublicArgs.properties文件配置
- 自定义函数（后续根据需要扩展即可）  
1、__random(var1,var2)，生成var1长度的随机数  
2、__phone():生成11位的手机号    
3、__idno():生成18位身份证号  
- 预期结果断言支持json格式及包含函数（不区分大小写）可结合使用。  
示例：$.status=200;$.data.accountNo=6230780501000877514;__contAIN(SUCCESS)
- 目前暂只支持post,get方式的请求。
- 案例抛异常则自动重试；
- 对于无需数据库断言的接口，参考发SendmsgYg.java例子
- 对于需要数据库断言的接口，则需将数据库断言xml的变量添加到map，参考OpenYg.java例子
- 接口测试类需继承SetUpTearDown类。  
示例：public class SendmsgYg extends SetUpTearDown {}
- 目前提供以下五种供数方式，通过以下方式使用。  
  1、查询数据池供数：${dp.sql(select accountNo from account where status = 1)}  
  2、查询数据库供数：${db.sql(select accountNo from account_card where status = 1)}  
  3、先接口请求，然后提取响应报文供数：${SendmsgYg.case023.post($.data.code)} 或 ${SendmsgYg.case023.get($.data.code)}  
  4、先接口请求，然后查询数据库供数：${SendmsgYg.case023.post.db.sql(select accountNo from M_account_card where status = 1)}  
  5、自定义函数
- 数据库及数据池操作通过以下方法来访问：  
数据池查询：CRUDData.selectData(sql,"DP")，返回多个值的话只取第一个    
数据池新增修改删除：CRUDData.cUDData(sql,"DP")  
数据库查询：CRUDData.selectData(sql,"DB")，返回多个值的话只取第一个    
数据库新增修改删除：CRUDData.cUDData(sql,"DB")  
- 其他功能后续可根据需要扩展，比如文件上传、加密、解密等。
