package io.onedev.server.web.page.oauth;

import org.apache.wicket.request.mapper.parameter.PageParameters;

public class MyOAuthApplicationPage extends OAuthApplicationPage {

	public MyOAuthApplicationPage(PageParameters parameters) {
		this(parameters, false);
	}

	protected MyOAuthApplicationPage(PageParameters parameters, boolean creating) {
		super(parameters, creating);
	}

}
