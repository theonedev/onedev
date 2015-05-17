package com.pmease.gitplex.web.page.security;

import com.pmease.gitplex.web.WebSession;
import com.pmease.gitplex.web.page.base.BasePage;

@SuppressWarnings("serial")
public class LogoutPage extends BasePage {

	public LogoutPage() {
		WebSession.get().logout();
		
        getSession().info("You've been logged out");
        
        setResponsePage(getApplication().getHomePage());
	}
	
}
