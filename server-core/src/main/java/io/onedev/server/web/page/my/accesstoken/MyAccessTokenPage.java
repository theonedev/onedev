package io.onedev.server.web.page.my.accesstoken;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.model.User;
import io.onedev.server.web.component.user.accesstoken.AccessTokenPanel;
import io.onedev.server.web.page.my.MyPage;

@SuppressWarnings("serial")
public class MyAccessTokenPage extends MyPage {

	public MyAccessTokenPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new AccessTokenPanel("accessToken") {
			
			@Override
			protected User getUser() {
				return getLoginUser();
			}
			
		});
		
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, "My Access Token");
	}

}
