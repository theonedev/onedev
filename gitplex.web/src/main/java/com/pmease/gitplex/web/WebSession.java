package com.pmease.gitplex.web;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.apache.wicket.request.Request;

public class WebSession extends org.apache.wicket.protocol.http.WebSession {

	private static final long serialVersionUID = 1L;

	public WebSession(Request request) {
		super(request);
	}

	public static WebSession get() {
		return (WebSession) org.apache.wicket.protocol.http.WebSession.get();
	}

	public void login(String userName, String password, boolean rememberMe) {
		Subject subject = SecurityUtils.getSubject();

		// Force a new session to prevent fixation attack.
		// We have to invalidate via both Shiro and Wicket; otherwise it doesn't
		// work.
		subject.getSession().stop();
		WebSession.get().replaceSession(); 

		UsernamePasswordToken token;
		token = new UsernamePasswordToken(userName, password, rememberMe);
		
		subject.login(token);
	}
	
	public void logout() {
		SecurityUtils.getSubject().logout();
        WebSession session = WebSession.get();
        session.replaceSession();
	}
}
