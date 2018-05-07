package com.demo.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 这个类用于进行基本的数据库操作，用户可以通过实现内部类（继承接口）来完成对数据库的操作，这样显得封装性更好
 */
public class MyDatabaseUtil  {
    private final String dbUrl = "jdbc:mysql://localhost:3306/jw_wechat?useSSL=false";
    private final String dbUser = "root";
    private final String dbPwd = "root";

    public MyDatabaseUtil(){
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private Connection getConnection() throws SQLException {
        return java.sql.DriverManager.getConnection(dbUrl,dbUser,dbPwd);
    }

    private void closeConnection(Connection conn)  {
        if(null != conn) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void closePrepStmt(PreparedStatement preparedStatement){
        if (null != preparedStatement) {
            try {
                preparedStatement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void closeResultSet(ResultSet resultSet)  {
        if (null != resultSet) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 执行一条SQL语句用于更新、插入或者删除数据，但是还没有加入防注入
     * @param s
     * @return
     */
    private Object executeSqlStringChange(String s){
        Connection conn = null;
        PreparedStatement preparedStatement = null;
        int i = 0;
        try {
            conn = getConnection();
            preparedStatement = conn.prepareStatement(s);
            i = preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closePrepStmt(preparedStatement);
            closeConnection(conn);
        }
        return i;
    }

    /**
     * 执行一条语句用于查询thing字段的数据元素，注意是第一行
     * @param s  查询语句
     * @param thing 列名
     * @return 第一行该列的数据
     */
    private Object executeSqlStringQuery(String s, String thing){
        Connection conn = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        String result = null;
        try {
            conn = getConnection();
            preparedStatement = conn.prepareStatement(s);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                result = resultSet.getString(thing);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResultSet(resultSet);
            closePrepStmt(preparedStatement);
            closeConnection(conn);
        }
        return result;
    }

    public MyDbUtil getQueryDb(){
        return new MyDbUtil() {
            public Object doDataWork(String[] s) {
                return executeSqlStringQuery(s[0],s[1]);
            }
        };
    }

    public MyDbUtil getChangeDb() {
        return new MyDbUtil() {
            public Object doDataWork(String[] s) {
                return executeSqlStringChange(s[0]);
            }
        };
    }


    public static void main(String [] args){
        MyDatabaseUtil databaseUtil = new MyDatabaseUtil();
        String query = "SELECT * FROM jw_user";
        try {
            String[] strings = {query,"user_id"};
            String s = (String) databaseUtil.getQueryDb().doDataWork(strings);
            System.out.println(s);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String[] change = {"update facility set facility_state = 0"};
        System.out.println(databaseUtil.getChangeDb().doDataWork(change));
    }
}
