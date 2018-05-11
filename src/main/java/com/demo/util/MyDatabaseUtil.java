package com.demo.util;

import java.sql.*;

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
        Object i = 0;
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
        Object result = null;
        try {
            conn = getConnection();
            preparedStatement = conn.prepareStatement(s);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                result = resultSet.getObject(thing);
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

    public MyDbUtil getMyDaUtilImpl(){
        return new MyDbUtil() {


            public Object doDataSelect(String s1, String s2) {
                return executeSqlStringQuery(s1, s2);
            }

            public Object doDataChange(String s) {
                return executeSqlStringChange(s);
            }
        };
    }



    public static void main(String [] args){
        String msgText = "unbind:fac";
        String fromUserName = "o1ETFw2Nrp8SVZPlYe0Zl6w0h9mw";
        Integer flag = null;
        MyDbUtil myDbUtil = new MyDatabaseUtil().getMyDaUtilImpl();
        String facility_name = msgText.substring(msgText.indexOf(':')+1);   //用户要解绑定的设备名
        //微信的文本应该是这样的：order:fac:1   order:设备名:命令状态
        String sql = "DELETE FROM facility WHERE fac_name = \"" + facility_name + "\" and user_id = \"" +
                fromUserName + "\"";
        System.out.println(sql);
        flag = (Integer)myDbUtil.doDataChange(sql);
        System.out.println(flag);
    }
}
