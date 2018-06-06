package com.demo.util;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import sun.rmi.runtime.Log;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class BodyDetect {

    private static final String API_KEY = "pB8MUjNa0uW2zzCd5uLIHl_vviK_PS0I";   //face++  API KEY
    private static final String API_SECRET = "Jc2dC6NYBfNAQp0RTBexGKWq8qDQCb2c";
    private static final String API_URL = "https://api-cn.faceplusplus.com/humanbodypp/v1/detect";


    /**
     * 向接口发送请求,返回JSON响应字符串,如果请求失败返回NULL
     * @param filePath 文件路径
     * @return
     */
    public static String postRequestFrBodyDetect(String filePath){
        String image_base64 = Image.GetImageStrFromPath(filePath);    //TODO 图片的base64编码
        OkHttpClient okHttpClient = new OkHttpClient();
        //Form表单格式的参数传递
        FormBody formBody = new FormBody
                .Builder()
                .add("api_key",API_KEY)
                .add("api_secret",API_SECRET)//设置参数名称和参数值
                .add("image_base64",image_base64)
                .build();
        Request request = new Request
                .Builder()
                .post(formBody)//Post请求的参数传递，此处是和Get请求相比，多出的一句代码</font>
                .url(API_URL)
                .build();
        try {
            Response response = okHttpClient.newCall(request).execute();
            String result = response.body().string();
            System.out.println(result);
            response.body().close();
            return spliceString(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 返回是否检测到有人，为1表示检测到有人，为0表示没有
     * @param result
     * @return
     */
    private static String spliceString(String result){
        String temp = null;
        String myResult = null;
        try {
            String flag1 = "\"humanbody_rectangle\": {";
            String flag2 = "}";
            temp = result.substring(result.indexOf(flag1) + flag1.length());
            myResult = temp.substring(1,temp.indexOf(flag2));
        } catch (Exception e) {
            return "false";
        }

        return myResult;
    }

    public static void main(String[] args){
        String result = postRequestFrBodyDetect("C:\\image\\jwweixin\\o1ETFw2Nrp8SVZPlYe0Zl6w0h9mwfac.jpg");

        return;
    }
}
