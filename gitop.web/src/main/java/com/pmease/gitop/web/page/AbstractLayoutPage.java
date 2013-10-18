package com.pmease.gitop.web.page;

import org.apache.shiro.SecurityUtils;

import com.google.common.base.Optional;
import com.pmease.gitop.core.model.User;

@SuppressWarnings("serial")
public abstract class AbstractLayoutPage extends BasePage {

	@Override
	protected void onPageInitialize() {
		super.onPageInitialize();
		add(new GlobalHeaderPanel("header"));
	}
	
	protected Optional<User> currentUser() {
	    return Optional.<User>fromNullable(User.getCurrent());
	}
	
	protected boolean isLoggedIn() {
		return currentUser().isPresent();
	}
	
	protected boolean isRemembered() {
		return SecurityUtils.getSubject().isRemembered();
	}
	
	protected boolean isAuthenticated() {
		return SecurityUtils.getSubject().isAuthenticated();
	}
}
