package io.onedev.server.web.page.admin.user.avatar;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.web.component.user.avataredit.AvatarEditPanel;
import io.onedev.server.web.page.admin.user.UserPage;

@SuppressWarnings("serial")
public class UserAvatarPage extends UserPage {
	
	public UserAvatarPage(PageParameters params) {
		super(params);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(new AvatarEditPanel("content", userModel));
	}

}
