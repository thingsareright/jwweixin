package com.demo.servlet;

import com.demo.util.MyDatabaseUtil;
import com.demo.util.MyDbUtil;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 这个是用来处理设备普通请求的
 */
public class FacilityServlet extends HttpServlet {

    private final String FAC_LIGHT_STATE = "fac_light_state";
    private final String FAC_LR_STATE = "fac_lr_state";
    private final String FAC_CAMERA_STATE = "fac_camera_state";
    private final String FAC_EM_STATE = "fac_em_state";

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
            logger.info(stream);
            for (int s: whatToDoForFac) {
                stream.write(s);
            }
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
        ints[0] = Integer.parseInt((myDbUtil.doDataSelect(sql,FAC_LIGHT_STATE).toString()));
        ints[1] = Integer.parseInt(myDbUtil.doDataSelect(sql,FAC_LR_STATE).toString());
        ints[2] = Integer.parseInt(myDbUtil.doDataSelect(sql,FAC_CAMERA_STATE).toString());
        ints[3] = Integer.parseInt(myDbUtil.doDataSelect(sql,FAC_EM_STATE).toString());
        return ints;
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
