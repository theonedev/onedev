package com.gitplex.server.web.page.security;

import org.apache.wicket.RestartResponseException;

import com.gitplex.server.security.SecurityUtils;
import com.gitplex.server.web.WebSession;
import com.gitplex.server.web.page.base.BasePage;

@SuppressWarnings("serial")
public class LogoutPage extends BasePage {

	public LogoutPage() {
		WebSession.get().logout();
		
		if (SecurityUtils.canAccessPublic())
			getSession().warn("You've been logged out");
        
        throw new RestartResponseException(getApplication().getHomePage());
	}
	
}
