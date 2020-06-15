package io.onedev.server.web.page.admin.user.accesstoken;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.model.User;
import io.onedev.server.web.component.user.accesstoken.AccessTokenPanel;
import io.onedev.server.web.page.admin.user.UserPage;

@SuppressWarnings("serial")
public class UserAccessTokenPage extends UserPage {

	public UserAccessTokenPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new AccessTokenPanel("accessToken") {
			
			@Override
			protected User getUser() {
				return UserAccessTokenPage.this.getUser();
			}
			
		});
	}

}
