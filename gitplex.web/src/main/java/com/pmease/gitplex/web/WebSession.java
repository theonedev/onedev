package com.pmease.gitplex.web;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.apache.wicket.request.Request;

import com.google.common.base.Optional;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.UserManager;
import com.pmease.gitplex.core.model.User;

public class WebSession extends org.apache.wicket.protocol.http.WebSession {

	private static final long serialVersionUID = 1L;

	private boolean displayOutline;
	
	public WebSession(Request request) {
		super(request);
	}

	public static WebSession get() {
		return (WebSession) org.apache.wicket.protocol.http.WebSession.get();
	}

	public static Optional<User> getCurrentUser() {
		return Optional.fromNullable(GitPlex.getInstance(UserManager.class).getCurrent());
	}
	
	public boolean isDisplayOutline() {
		return displayOutline;
	}

	public void setDisplayOutline(boolean displayOutline) {
		this.displayOutline = displayOutline;
	}

	public void runAs(User user) {
		Subject subject = SecurityUtils.getSubject();
		subject.getSession().stop();
		WebSession.get().replaceSession(); 
		
		subject.runAs(user.getPrincipals());
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
}
