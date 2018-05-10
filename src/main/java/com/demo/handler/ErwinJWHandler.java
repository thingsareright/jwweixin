package com.demo.handler;

import com.demo.util.MyDatabaseUtil;
import com.demo.util.MyDbUtil;
import com.jw.api.MaterialApi;
import com.jw.api.impl.DefaultMateralApi;
import com.jw.bean.msg.WeChatImageBean;
import com.jw.bean.msg.WeChatMsgTextBean;
import com.jw.bean.response.WeChatResponseBaseBean;
import com.jw.factory.ResponseMsgBuilder;
import com.jw.handler.impl.DefaultMsgReceiveHandler;

import java.io.File;
import java.sql.Timestamp;

public class ErwinJWHandler extends DefaultMsgReceiveHandler{

    public final static int ACTION_IROFF_CAMERAOFF = 0;
    public final static int ACTION_IRON_CAMERAOFF = 1;
    public final static int ACTION_IROFF_CAMERAON = 2;
    public final static int ACTION_IRON_CAMERAON = 3;

    private  MaterialApi materialApi = new DefaultMateralApi();

    @Override
    public WeChatResponseBaseBean onReceiveMsgText(WeChatMsgTextBean msgBean, ResponseMsgBuilder builder) {
        String msgText = msgBean.getContent();      //用户发来的信息
        String fromUserName = msgBean.getFromUserName();    //用户ID
        String kindOfOrder = msgText.substring(0, msgText.indexOf(':'));//用户冒号前的指令信息

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
            flag = doOrderAction(msgText);
            if(1 == flag)
                return builder.buildResponseTextBean("Order Get!");
            else
                return builder.buildResponseTextBean("Order failed!");
        }
        return builder.buildResponseTextBean("failed!");
    }

    private int doOrderAction(String msgText) {
        //微信的文本应该是这样的：bind:fac:1   bind:设备名:命令状态
        MyDbUtil myDbUtil = new MyDatabaseUtil().getMyDaUtilImpl();
        String temp = msgText.substring(msgText.indexOf(':'));
        String facility_name = temp.substring(temp.indexOf(':')+1,temp.indexOf(':',1));
        String facility_new_state = temp.substring(temp.indexOf(':',facility_name.length())+1);
        final int new_state = Integer.parseInt(facility_new_state);
        String sql = "UPDATE facility SET fac_new_state = " + facility_new_state + " where fac_name = \"" + facility_name + "\"";
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
        Integer flag = ((Integer) myDbUtil.doDataSelect(sql, "incre_id")).intValue();
        if (null == flag){
            int secFlag = ((Integer)myDbUtil.doDataChange("insert into jw_user(user_id) values(\"" + fromUserName + "\")")).intValue();
            if(1 == secFlag)
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
            sql = "INSERT INTO facility(user_id,fac_id,fac_name,fac_old_state,fac_kind) VALUES(\"" + fromUserName + "\"," +
                    facility_id + ",\"" + facility_name + "\"," + 0 + "," + 1 + ")";
            flag = (Integer)myDbUtil.doDataChange(sql);
            if(1 == flag)
                return 1;   //绑定成功
        }else
            return 3;   //设备已经被绑定

        return 0;   //绑定失败
    }
}
