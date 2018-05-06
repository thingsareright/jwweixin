package com.demo.config;

import com.demo.handler.ErwinJWHandler;
import com.jw.config.impl.SimpleJwConfig;
import com.jw.handler.MsgReceiveHandler;

public class ErwinJWConfig extends SimpleJwConfig {

    public String getToken() {
        return "";
    }

    public String getAppId() {
        return "wxda4d2170a20acad2";
    }

    public String getAppSecret() {
        return "be2bdca0bcc754ba0f03f799fc3761f8";
    }

    @Override
    public MsgReceiveHandler getMsgReceiveHandler() {
        return new ErwinJWHandler();
    }
}
