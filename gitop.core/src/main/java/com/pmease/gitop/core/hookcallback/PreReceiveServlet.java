package com.pmease.gitop.core.hookcallback;

import javax.inject.Singleton;

@SuppressWarnings("serial")
@Singleton
public class PreReceiveServlet extends CallbackServlet {

    public static final String PATH = "/git-pre-receive";

    @Override
    protected void callback(String callbackData, Output output) {
        output.writeLine("hello world");
        output.writeLine("just do it");
    }
    
}
