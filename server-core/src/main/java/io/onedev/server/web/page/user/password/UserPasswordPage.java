package io.onedev.server.web.page.user.password;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.web.component.user.passwordedit.PasswordEditPanel;
import io.onedev.server.web.page.user.UserPage;

public class UserPasswordPage extends UserPage {
	
	public UserPasswordPage(PageParameters params) {
		super(params);
		if (getUser().isServiceAccount() || getUser().isDisabled())
			throw new IllegalStateException();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(new WebMarkupContainer("authViaExternalSystemNotice").setVisible(getUser().getPassword() == null));
		add(new PasswordEditPanel("content", userModel));
	}

}
