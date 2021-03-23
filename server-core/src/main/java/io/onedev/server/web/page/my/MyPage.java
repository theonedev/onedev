package io.onedev.server.web.page.my;

import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.web.page.layout.LayoutPage;
import io.onedev.server.web.page.simple.security.LoginPage;

@SuppressWarnings("serial")
public abstract class MyPage extends LayoutPage {
	
	public MyPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		if (getLoginUser() == null) 
			throw new RestartResponseAtInterceptPageException(LoginPage.class);
	}

	@Override
	protected String getPageTitle() {
		return "My - OneDev";
	}
	
}
