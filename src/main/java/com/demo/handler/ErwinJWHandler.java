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
        }
        return builder.buildResponseTextBean("failed!");
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
}
