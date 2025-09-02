package io.onedev.server.web.page.security;

import io.onedev.server.web.WebSession;
import io.onedev.server.web.page.base.BasePage;

import static io.onedev.server.web.translation.Translation._T;

import org.apache.wicket.RestartResponseException;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class LogoutPage extends BasePage {

	public LogoutPage(PageParameters params) {
		super(params);
		WebSession.get().logout();
		getSession().warn(_T("You've been logged out"));
		throw new RestartResponseException(getApplication().getHomePage());
	}
	
}
