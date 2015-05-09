package com.pmease.gitplex.web.page;

import org.apache.shiro.SecurityUtils;

@SuppressWarnings("serial")
public abstract class LayoutPage extends BasePage {

	@Override
	protected void onInitialize() {
		super.onInitialize();
	}
	
	protected boolean isLoggedIn() {
		return getCurrentUser() != null;
	}
	
	protected boolean isRemembered() {
		return SecurityUtils.getSubject().isRemembered();
	}
	
	protected boolean isAuthenticated() {
		return SecurityUtils.getSubject().isAuthenticated();
	}
	
}
