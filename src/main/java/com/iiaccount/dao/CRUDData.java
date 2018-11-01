package com.iiaccount.dao;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.sql.*;

public class CRUDData {
    static Logger log = Logger.getLogger(CRUDData.class);

    static Connection connection = null;
    static ResultSet result = null;

    /*
     * 查询
     * 只针对select a from talbe 类型的语句；
     * select * from 或 select a,b from等暂时用不到，暂不支持；
     */
    public static String selectData(String sql,String flag) throws SQLException, IOException, ClassNotFoundException {
        String value = "";
        if(flag.toLowerCase().equals("db"))
            connection = DBDPConnection.getDBConnection();
        else
            connection = DBDPConnection.getDPConnection();

        Statement statement = connection.createStatement();
        result = statement.executeQuery(sql);

        if (result.next()) { //如果存在多个值，只取第一个
            value = result.getString(1);
        }
        log.info(sql+"取值为："+value);

        if (result != null)
            result.close();
        if (connection != null)
            connection.close();

        return value;
    }

    //新增，修改，删除
    public static void cUDData(String sql,String flag) throws SQLException, IOException, ClassNotFoundException {

        if(flag.toLowerCase().equals("db"))
            connection = DBDPConnection.getDBConnection();
        else
            connection = DBDPConnection.getDPConnection();

        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.executeUpdate();

        log.info("新增/修改/删除语句："+sql);

        if (connection != null){
            preparedStatement.close();
            connection.close();
        }

    }
}
