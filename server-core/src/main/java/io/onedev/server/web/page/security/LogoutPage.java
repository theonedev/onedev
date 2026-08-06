package io.onedev.server.web.page.security;

import io.onedev.server.web.WebSession;
import io.onedev.server.web.page.base.BasePage;

import static io.onedev.server.web.translation.Translation._T;

import org.apache.wicket.RestartResponseException;
import org.apache.wicket.request.flow.RedirectToUrlException;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class LogoutPage extends BasePage {

	public LogoutPage(PageParameters params) {
		super(params);
		var session = WebSession.get();
		var ssoLogoutUrl = session.getSsoLogoutUrl();
		session.logout();
		session.warn(_T("You've been logged out"));
		if (ssoLogoutUrl != null)
			throw new RedirectToUrlException(ssoLogoutUrl);
		else
			throw new RestartResponseException(getApplication().getHomePage());
	}
	
}
