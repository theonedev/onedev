package io.onedev.server.web.page.user.profile;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.web.component.user.profileedit.ProfileEditPanel;
import io.onedev.server.web.page.user.UserPage;

public class UserProfilePage extends UserPage {

	public UserProfilePage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(new ProfileEditPanel("content", userModel));
	}
		
}
