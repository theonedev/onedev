package com.pmease.gitop.core.hookcallback;

import javax.inject.Singleton;

@SuppressWarnings("serial")
@Singleton
public class PostReceiveServlet extends CallbackServlet {

    public static final String PATH = "/git-post-receive";
    
    @Override
    protected void callback(String callbackData, Output output) {
    }
    
}
