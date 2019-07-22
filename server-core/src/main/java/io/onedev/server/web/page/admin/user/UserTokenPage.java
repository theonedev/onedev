package io.onedev.server.web.page.admin.user;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.web.component.user.tokengenerate.TokenGeneratePanel;

@SuppressWarnings("serial")
public class UserTokenPage extends UserPage {
	
	public UserTokenPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new TokenGeneratePanel("content", userModel));
	}
	
}
