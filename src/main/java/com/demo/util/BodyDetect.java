package com.demo.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.JsonObject;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import sun.rmi.runtime.Log;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    public static String postRequestForBodyDetect(String filePath){
        try {
            String image_base64 = Image.GetImageStrFromPath(filePath);
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

            Response response = okHttpClient.newCall(request).execute();
            String result = response.body().string();
            System.out.println(result);
            response.body().close();
            HumanBody_Rect humanBody_rect = jsonToHumanBody_Rect(result);   //如果返回null，那会调到catch,然后返回错误条件下应该返回的JSON格式串
            Humanbody_Rectangle humanbody_rectangle = humanBody_rect.getHumanbodies().get(0).getHumanbody_rectangle();
            humanbody_rectangle.setState(1);
            return JSON.toJSONString(humanbody_rectangle);
        } catch (Exception e) {
            return JSON.toJSONString(new Human_Detect_Fail(0));
        }
    }

    /**
     * 与spliceString不同，这里采用JSON格式转换进行
     * @param result
     * @return
     */
    private static HumanBody_Rect jsonToHumanBody_Rect(String result) {
        HumanBody_Rect humanbody_rectangles = new HumanBody_Rect();
        try {
            if (null != result){
                humanbody_rectangles = JSON.parseObject(result, HumanBody_Rect.class);
                return humanbody_rectangles;
            }

        } catch (Exception e) {
            return null;
        } finally {

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
            if (result.indexOf(flag1) <= 0)
                return null;
            temp = result.substring(result.indexOf(flag1) + flag1.length() -1);
            if (temp == null || temp.equals(""))
                return null;
            myResult = temp.substring(1,temp.indexOf(flag2));
        } catch (Exception e) {
            return null;
        }

        return myResult;
    }

    public static void main(String[] args){
        String result = postRequestForBodyDetect(("C:\\image\\jwweixin\\o1ETFw2Nrp8SVZPlYe0Zl6w0h9mwfac.jpg"));
        System.out.println(result);
        return;
    }
}

/**
 * humanbodies里的一个元素
 */
class HumanBodies_Element{
    private Humanbody_Rectangle humanbody_rectangle;
    private float confidence;



    public HumanBodies_Element() {
    }

    public Humanbody_Rectangle getHumanbody_rectangle() {
        return humanbody_rectangle;
    }

    public void setHumanbody_rectangle(Humanbody_Rectangle humanbody_rectangle) {
        this.humanbody_rectangle = humanbody_rectangle;
    }

    public float getConfidence() {
        return confidence;
    }

    public void setConfidence(float confidence) {
        this.confidence = confidence;
    }
}

class HumanBody_Rect implements Serializable{
    private String image_id;
    private String request_id;
    private int time_used;
    private List<HumanBodies_Element> humanbodies;

    public HumanBody_Rect() {
    }

    public String getImage_id() {
        return image_id;
    }

    public void setImage_id(String image_id) {
        this.image_id = image_id;
    }

    public String getRequest_id() {
        return request_id;
    }

    public void setRequest_id(String request_id) {
        this.request_id = request_id;
    }

    public int getTime_used() {
        return time_used;
    }

    public void setTime_used(int time_used) {
        this.time_used = time_used;
    }

    public List<HumanBodies_Element> getHumanbodies() {
        return humanbodies;
    }

    public void setHumanbodies(List<HumanBodies_Element> humanbodies) {
        this.humanbodies = humanbodies;
    }
}

/**
 * humanbodies里的一个元素里的一个humanbody_rectangle
 */
class Humanbody_Rectangle{
    private int state;
    private int width;
    private int top;
    private int height;
    private int left;

    public Humanbody_Rectangle() {
    }

    public Humanbody_Rectangle(int width, int top, int height, int left) {
        this.width = width;
        this.top = top;
        this.height = height;
        this.left = left;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getTop() {
        return top;
    }

    public void setTop(int top) {
        this.top = top;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getLeft() {
        return left;
    }

    public void setLeft(int left) {
        this.left = left;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
}


class Human_Detect_Fail{
    private int state;

    public Human_Detect_Fail(int state) {
        this.state = state;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
}