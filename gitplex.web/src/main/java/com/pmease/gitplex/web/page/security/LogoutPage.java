package com.pmease.gitplex.web.page.security;

import org.apache.shiro.SecurityUtils;

import com.pmease.gitplex.web.WebSession;
import com.pmease.gitplex.web.page.base.BasePage;

@SuppressWarnings("serial")
public class LogoutPage extends BasePage {

	public LogoutPage() {
		SecurityUtils.getSubject().logout();
        WebSession session = WebSession.get();
        session.replaceSession();
    
        session.info("You've been logged out");
        
        setResponsePage(getApplication().getHomePage());
	}
	
}
