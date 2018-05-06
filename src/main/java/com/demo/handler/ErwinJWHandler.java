package com.demo.handler;

import com.jw.api.MaterialApi;
import com.jw.api.impl.DefaultMateralApi;
import com.jw.bean.msg.WeChatImageBean;
import com.jw.bean.msg.WeChatMsgTextBean;
import com.jw.bean.response.WeChatResponseBaseBean;
import com.jw.factory.ResponseMsgBuilder;
import com.jw.handler.impl.DefaultMsgReceiveHandler;

import java.io.File;

public class ErwinJWHandler extends DefaultMsgReceiveHandler{

    private  MaterialApi materialApi = new DefaultMateralApi();

    @Override
    public WeChatResponseBaseBean onReceiveMsgText(WeChatMsgTextBean msgBean, ResponseMsgBuilder builder) {
        return builder.buildResponseImageBean(msgBean.getContent());
    }

    @Override
    public WeChatResponseBaseBean onReceiveMsgImage(WeChatImageBean msgBean, ResponseMsgBuilder builder) {
        return builder.buildResponseTextBean(materialApi.addTempMedia(MaterialApi.IMAGE, new File("G:\\timg.jpg")));
    }
}
