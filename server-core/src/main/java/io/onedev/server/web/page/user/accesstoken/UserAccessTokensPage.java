package io.onedev.server.web.page.user.accesstoken;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.model.User;
import io.onedev.server.web.component.user.accesstoken.AccessTokenListPanel;
import io.onedev.server.web.page.user.UserPage;

public class UserAccessTokensPage extends UserPage {

	public UserAccessTokensPage(PageParameters params) {
		super(params);
		if (getUser().isDisabled())
			throw new IllegalStateException();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new AccessTokenListPanel("accessTokens") {
			
			@Override
			protected User getUser() {
				return UserAccessTokensPage.this.getUser();
			}
			
		});
	}

}
