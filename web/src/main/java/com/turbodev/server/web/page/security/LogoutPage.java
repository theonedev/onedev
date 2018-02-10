package com.turbodev.server.web.page.security;

import org.apache.wicket.RestartResponseException;

import com.turbodev.server.security.SecurityUtils;
import com.turbodev.server.web.WebSession;
import com.turbodev.server.web.page.base.BasePage;

@SuppressWarnings("serial")
public class LogoutPage extends BasePage {

	public LogoutPage() {
		WebSession.get().logout();
		
		if (SecurityUtils.canAccessPublic())
			getSession().warn("You've been logged out");
        
        throw new RestartResponseException(getApplication().getHomePage());
	}
	
}
