package com.demo.util;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

import java.util.HashMap;
import java.util.Map;

public class WeatherUtil {

    /**
     * 调用API返回天气，1为要收衣服,0为不需要收衣服
     * @return
     */
    public static int getWeatherState () {
        String host = "http://freecityid.market.alicloudapi.com";
        String path = "/whapi/json/alicityweather/briefforecast3days";
        String method = "POST";
        String appcode = "a716a3e807084812b39198f38b1cc2ed";
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Authorization", "APPCODE " + appcode);
        //根据API的要求，定义相对应的Content-Type
        headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        Map<String, String> querys = new HashMap<String, String>();
        Map<String, String> bodys = new HashMap<String, String>();
        bodys.put("cityId", "300");
        bodys.put("token", "677282c2f1b3d718152c4e25ed434bc4");


        try {

            HttpResponse response = HttpUtils.doPost(host, path, method, headers, querys, bodys);
            System.out.println(response.toString());
            //获取response的body
            String jsonBody = EntityUtils.toString(response.getEntity());
            System.out.println(jsonBody);
            //获取天气
            String weatherString = splitData(jsonBody, "\"conditionDay\":\"","\",");
            if(weatherString.contains("雨"))
                return 1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static String splitData(String str, String strStart, String strEnd) {
        String tempStr;
        tempStr = str.substring(str.indexOf(strStart) + strStart.length(), str.indexOf(strEnd,str.indexOf(strStart) + strStart.length()));
        return tempStr;
    }

    public static void main (String[] args){
        int i = getWeatherState();
        System.out.println(i);
    }

}
