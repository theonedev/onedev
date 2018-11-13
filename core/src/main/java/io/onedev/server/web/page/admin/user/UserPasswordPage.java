package io.onedev.server.web.page.admin.user;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.web.component.user.passwordedit.PasswordEditPanel;

@SuppressWarnings("serial")
public class UserPasswordPage extends UserPage {
	
	public UserPasswordPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new PasswordEditPanel("content", userModel));
	}
	
}
