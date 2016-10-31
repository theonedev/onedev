package com.gitplex.web.page.security;

import org.apache.wicket.RestartResponseException;

import com.gitplex.web.WebSession;
import com.gitplex.web.page.base.BasePage;

@SuppressWarnings("serial")
public class LogoutPage extends BasePage {

	public LogoutPage() {
		WebSession.get().logout();
		
        getSession().warn("You've been logged out");
        
        throw new RestartResponseException(getApplication().getHomePage());
	}
	
}
