package com.demo.handler;

import com.demo.util.ConstantUtil;
import com.demo.util.MyDatabaseUtil;
import com.demo.util.MyDbUtil;
import com.jw.api.MaterialApi;
import com.jw.api.impl.DefaultMateralApi;
import com.jw.bean.msg.WeChatImageBean;
import com.jw.bean.msg.WeChatMsgTextBean;
import com.jw.bean.response.WeChatResponseBaseBean;
import com.jw.bean.response.WeChatResponseTextBean;
import com.jw.factory.ResponseMsgBuilder;
import com.jw.handler.impl.DefaultMsgReceiveHandler;
import org.apache.commons.logging.Log;

import java.io.File;
import java.sql.Timestamp;

public class ErwinJWHandler extends DefaultMsgReceiveHandler{

    public final static int ACTION_IROFF_CAMERAOFF = 0;
    public final static int ACTION_IRON_CAMERAOFF = 1;
    public final static int ACTION_IROFF_CAMERAON = 2;
    public final static int ACTION_IRON_CAMERAON = 3;

    private  MaterialApi materialApi = new DefaultMateralApi();
    private static final String HELP_MSG = "欢迎！\r\n" +
            "注册请输入：    register:\r\n"
            + "绑定设备请输入：  bind:设备号:设备名\r\n" +
            "解除绑定设备请输入： unbind:设备名\r\n" +
            "查看某设备拍的最新的照片：picture:fac   picture:设备名\r\n" +
            "更改设备状态请输入： order:fac:1   order:设备名:命令状态\r\n" +
            "设备状态代码意义：\r\n" +
                    "0：关闭红外和摄像头\r\n" +
            "1:开启红外关闭摄像头\r\n" +
            "2:关闭红外开启摄像头\r\n" +
            "3:开启红外和摄像头\r\n" +
            "注意：所用冒号均为英文输入的冒号，且不可省略。";

    @Override
    public WeChatResponseBaseBean onReceiveMsgText(WeChatMsgTextBean msgBean, ResponseMsgBuilder builder) {
        String msgText = msgBean.getContent();      //用户发来的信息
        String fromUserName = msgBean.getFromUserName();    //用户ID
        String kindOfOrder; //用户命令
        try {
            kindOfOrder = msgText.substring(0, msgText.indexOf(':'));//用户冒号前的指令信息
        } catch (Exception e){
            return builder.buildResponseTextBean("没有冒号啊小伙子小姑娘，我给你讲讲道理：\r\n" + HELP_MSG);
        }


        if("register".equals(kindOfOrder)){
            int flag = doRegisterAction(fromUserName);
            if(1 == flag) {
                return builder.buildResponseTextBean("Register Successfully!");
            }else if(0 == flag){
                return builder.buildResponseTextBean("Already register!");
            }else {
                return builder.buildResponseTextBean("Register failed!");
            }
        } else if("bind".equals(kindOfOrder)) {
            //微信的文本应该是这样的：bind:123456:camera   bind:设备号:设备名
            String temp = msgText.substring(msgText.indexOf(':'));
            String facility_id = temp.substring(temp.indexOf(':')+1,temp.indexOf(':',1));
            String facility_name = temp.substring(temp.indexOf(':',facility_id.length())+1);
            int flag = doBindAction(facility_id,facility_name,fromUserName);
            if(1 == flag)
                return builder.buildResponseTextBean("bind " + facility_id + " successfully!");
            else if (2 == flag)
                return builder.buildResponseTextBean("Oh! You haven't registered! Please do it before you bind our facilities!");
            else if (3 == flag)
                return builder.buildResponseTextBean("Sorry! The facility has already been binded!");
            else
                return builder.buildResponseTextBean("Bind failed! Please try it again!");
        } else if("order".equals(kindOfOrder)){
            int flag = 0;
            flag = doOrderAction(msgText,fromUserName);
            if(1 == flag)
                return builder.buildResponseTextBean("Order Get!");
            else
                return builder.buildResponseTextBean("Order failed!");
        } else if ("unbind".equals(kindOfOrder)) {
            int flag = doUnBindAction(msgText,msgBean.getFromUserName());
            //微信的文本应该是这样的：unbind:camera   bind:设备名
            if(1 == flag)
                return builder.buildResponseTextBean("Unbind facility successfully!");
            else
                return builder.buildResponseTextBean("You fail to unbind the facility!");
        } else if("help".equals(kindOfOrder)){
            return builder.buildResponseTextBean(HELP_MSG);
        } else if("picture".equals(kindOfOrder)){
            //picture:fac
            String filePath;
            filePath = doPictureAction(msgText, msgBean.getFromUserName());
            WeChatResponseTextBean media = builder.buildResponseTextBean(materialApi.addTempMedia(MaterialApi.IMAGE, new File(filePath)));
            String tempMediaCode = media.getContent();
            return builder.buildResponseImageBean(tempMediaCode);
        } else {
            return builder.buildResponseTextBean(HELP_MSG);
        }
    }

    private String doPictureAction(String msgText, String fromUserName) {
        String facility_name = msgText.substring(msgText.indexOf(':') + 1);
        String filePath = ConstantUtil.IMAGE_DIR + "/" + facility_name + fromUserName + ".jpg";
        return filePath;
    }

    private int doUnBindAction(String msgText,String fromUserName) {
        Integer flag = null;
        MyDbUtil myDbUtil = new MyDatabaseUtil().getMyDaUtilImpl();
        String facility_name = msgText.substring(msgText.indexOf(':')+1);   //用户要解绑定的设备名
        String sql = "DELETE FROM facility WHERE fac_name = \"" + facility_name + "\" and user_id = \"" +
                fromUserName + "\"";
        flag = (Integer)myDbUtil.doDataChange(sql);
        if (null == flag)
            return 0;
        if(1 == flag)
            return 1;   //解除绑定成功
        return 0;
    }

    private int doOrderAction(String msgText,String fromUserName) {
        //微信的文本应该是这样的：order:fac:light:1   order:设备名:设备功能类型:设备状态改变
        MyDbUtil myDbUtil = new MyDatabaseUtil().getMyDaUtilImpl();
        String temp = msgText.substring(msgText.indexOf(':'));
        String facility_name = temp.substring(1,temp.indexOf(':',1));
        temp = temp.substring(temp.indexOf(':',facility_name.length())+1);
        String fac_action_type = temp.substring(0,temp.indexOf(':'));
        String fac_action_state = temp.substring(temp.indexOf(':')+1);
        String sql = null;
        if("light".equals(fac_action_type)){
            sql = "UPDATE facility SET fac_light_state = " + fac_action_state + " ";
        } else if ("lr".equals(fac_action_type)) {
            //红外
            sql = "UPDATE facility SET fac_lr_state = " + fac_action_state + " ";
        } else if("camera".equals(fac_action_type)) {
            sql = "UPDATE facility SET fac_camera_state = "+ fac_action_state + " ";
        } else if("em".equals(fac_action_type)){
            //电机
            sql = "UPDATE facility SET fac_em_state = " + fac_action_state + " ";
        }
        sql += "WHERE fac_name = \"" + facility_name + "\" and user_id = \"" + fromUserName + "\"";
        Integer flag = (Integer) myDbUtil.doDataChange(sql);
        if(null == flag)
            return 2;   //更新失败
        return 1;   //更新成功
    }


    @Override
    public WeChatResponseBaseBean onReceiveMsgImage(WeChatImageBean msgBean, ResponseMsgBuilder builder) {
        return builder.buildResponseTextBean(materialApi.addTempMedia(MaterialApi.IMAGE, new File("G:\\timg.jpg")));
    }

    private int doRegisterAction(final String fromUserName) //先判断数据库里有没有存储相关信息，没有的话就自动的将用户信息存到数据库里
    {
        String sql = "SELECT * FROM jw_user WHERE user_id =  \"" + fromUserName + "\"";  //TODO 没有添加SQL防注入
        MyDatabaseUtil databaseUtil = new MyDatabaseUtil();
        MyDbUtil myDbUtil = databaseUtil.getMyDaUtilImpl();
        Integer flag = ((Integer) myDbUtil.doDataSelect(sql, "incre_id"));
        if (null == flag){
            Integer secFlag = ((Integer)myDbUtil.doDataChange("insert into jw_user(user_id) values(\"" + fromUserName + "\")")).intValue();
            if(secFlag != null && 1 == secFlag)
                return 1;   //注册成功
        }else if(1 <= flag.intValue())
            return 0;   //已经注册
        else{
            int secFlag = ((Integer)myDbUtil.doDataChange("insert into jw_user(user_id) values(\"" + fromUserName + "\")")).intValue();
            if(1 == secFlag)
                return 1;   //注册成功
        }
        return 2;   //注册失败
    }

    /**
     * 判断数据库里有没有该设备的绑定信息，有的话就放弃绑定，否则再进行绑定
     * @param facility_id
     * @param facility_name
     * @param fromUserName
     * @return
     */
    private int doBindAction(String facility_id, String facility_name, String fromUserName) {
        //先判断是否已经注册了
        Integer flag = doRegisterAction(fromUserName);
        if (2 == flag)
            return 2;   //绑定设备时发现注册失败
        //这个时候已经注册了，现在我们开始绑定设备
        MyDbUtil myDbUtil = new MyDatabaseUtil().getMyDaUtilImpl();
        String sql = "SELECT * FROM facility WHERE fac_id = \"" + facility_id + "\"";
        flag = (Integer) myDbUtil.doDataSelect(sql,"incre_id");
        if (null == flag){
            sql = "INSERT INTO facility(user_id,fac_id,fac_name) VALUES(\"" + fromUserName + "\"," +
                    facility_id + ",\"" + facility_name + "\")";
            flag = (Integer)myDbUtil.doDataChange(sql);
            if (null == flag)
                return 0;   //防止BUG
            if(1 == flag)
                return 1;   //绑定成功
        }else
            return 3;   //设备已经被绑定

        return 0;   //绑定失败
    }
}
