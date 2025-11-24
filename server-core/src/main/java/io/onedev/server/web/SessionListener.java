package io.onedev.server.web;

public interface SessionListener {
	
    void sessionCreated(String sessionId);
    
    void sessionDestroyed(String sessionId);

}