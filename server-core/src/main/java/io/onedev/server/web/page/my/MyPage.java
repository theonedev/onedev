package io.onedev.server.web.page.my;

import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.service.SettingService;
import io.onedev.server.model.User;
import io.onedev.server.web.page.layout.LayoutPage;
import io.onedev.server.web.page.security.LoginPage;
import io.onedev.server.web.util.UserAware;

public abstract class MyPage extends LayoutPage implements UserAware {
	
	public MyPage(PageParameters params) {
		super(params);
		if (getUser() == null) 
			throw new RestartResponseAtInterceptPageException(LoginPage.class);
	}

	@Override
	public User getUser() {
		return getLoginUser();
	}

	@Override
	protected String getPageTitle() {
		return "My - " + OneDev.getInstance(SettingService.class).getBrandingSetting().getName();
	}
	
}
