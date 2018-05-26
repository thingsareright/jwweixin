package com.demo.servlet;

import com.demo.util.MyDatabaseUtil;
import com.demo.util.MyDbUtil;
import com.demo.util.WeatherUtil;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * 这个是用来处理设备普通请求的
 */
public class FacilityServlet extends HttpServlet {

    private final String FAC_LIGHT_STATE = "fac_light_state";   //设备灯的状态
    private final String FAC_LR_STATE = "fac_lr_state"; //设备红外开关状态
    private final String FAC_CAMERA_STATE = "fac_camera_state"; //设备摄像头开关状态
    private final String FAC_EM_STATE = "fac_em_state"; //设备电机开关状态

    private final Logger logger = Logger.getLogger(FacilityServlet.class);

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        super.doPost(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String fac_id = request.getParameter("fac_id");

        response.setContentType("text/html");
        response.setCharacterEncoding("UTF8");
        try {
            int[] whatToDoForFac = new int[5];  //用于指示单片机应该干什么，具体包括灯光、红外、摄像头、电机
            whatToDoForFac = doSelectForWhatToDo(fac_id);
            OutputStream stream = response.getOutputStream();
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(stream));
            logger.info(stream);
            for (int s: whatToDoForFac) {
                bufferedWriter.write(s + "");
            }
            bufferedWriter.flush();
            stream.close();
        } catch (NumberFormatException e) {
            logger.error("Facid: " + fac_id + " has an error.", e);
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
            logger.error("Facid: " + fac_id + " has an error.", e);
        } finally{
            logger.info("Facid: " + fac_id + " has a request!");
        }
    }

    private int[] doSelectForWhatToDo(String fac_id) {
        String sql = "SELECT * FROM facility WHERE fac_id = " + fac_id + "";
        logger.info(sql);
        MyDbUtil myDbUtil = new MyDatabaseUtil().getMyDaUtilImpl();
        int[] ints = new int[5];
        try {
            ints[0] = Integer.parseInt((myDbUtil.doDataSelect(sql,FAC_LIGHT_STATE).toString()));    //为0表示设备上的灯应该是关着的状态，为1表示设备上灯应该是开着的状态
            ints[1] = Integer.parseInt(myDbUtil.doDataSelect(sql,FAC_LR_STATE).toString());
            ints[2] = Integer.parseInt(myDbUtil.doDataSelect(sql,FAC_CAMERA_STATE).toString());
            ints[3] = Integer.parseInt(myDbUtil.doDataSelect(sql,FAC_EM_STATE).toString());
            //通过最后的设置，来增加天气API的权限
            //ints[3] = WeatherUtil.getWeatherState();
            return ints;
        } catch (Exception e){
            e.printStackTrace();
            for(int i =0; i<ints.length; i++)
                ints[i] = 0;
            return ints;
        }
    }

    /**
     * 这个方法用于把字符串参数转换为整数，主要用于处理单片机的请求
     * @param s 字符串
     * @return
     */
    private int stringToInt(String s) throws NumberFormatException{
        return Integer.parseInt(s);
    }
}
