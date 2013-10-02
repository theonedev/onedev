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
	
	protected boolean isSignedIn() {
		return SecurityUtils.getSubject().isAuthenticated();
	}
	
	protected Optional<User> currentUser() {
		User user = User.getCurrent();
		if (user.isAnonymous()) {
			return Optional.<User>absent();
		} else {
			return Optional.of(user);
		}
	}
}
