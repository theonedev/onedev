package io.onedev.server.web.page.my;

import org.apache.wicket.RestartResponseAtInterceptPageException;

import io.onedev.server.web.page.layout.LayoutPage;
import io.onedev.server.web.page.security.LoginPage;

@SuppressWarnings("serial")
public abstract class MyPage extends LayoutPage {
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		if (getLoginUser() == null) 
			throw new RestartResponseAtInterceptPageException(LoginPage.class);
	}

}
