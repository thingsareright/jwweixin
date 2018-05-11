package test;

import com.demo.util.ConstantUtil;

public class TestFun {
    public static void main(String[] args){
        String msgText = "fpictureac";
        String facility_name = msgText.substring(0,msgText.indexOf(':'));

        System.out.println(facility_name);
    }
}
