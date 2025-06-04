package io.onedev.server.web.page.user.basicsetting;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.web.component.user.basicsetting.BasicSettingPanel;
import io.onedev.server.web.page.user.UserPage;

public class UserBasicSettingPage extends UserPage {

	public UserBasicSettingPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(new BasicSettingPanel("content", userModel));
	}
		
}
