package test;

import com.demo.util.ConstantUtil;

public class TestFun {
    public static void main(String[] args){
        String sql = "UPDATE facility SET fac_camera_state = "+ 1 + " ";
        sql += "WHERE fac_name = \"" + "fac" + "\" and user_id = \"" + "dad" + "\"";
        System.out.println(sql);



    }
}
