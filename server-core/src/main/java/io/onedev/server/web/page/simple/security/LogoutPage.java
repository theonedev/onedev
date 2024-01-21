package io.onedev.server.web.page.simple.security;

import io.onedev.server.web.WebSession;
import io.onedev.server.web.page.base.BasePage;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.request.mapper.parameter.PageParameters;

@SuppressWarnings("serial")
public class LogoutPage extends BasePage {

	public LogoutPage(PageParameters params) {
		super(params);
		WebSession.get().logout();
		getSession().warn("You've been logged out");
		throw new RestartResponseException(getApplication().getHomePage());
	}
	
}
