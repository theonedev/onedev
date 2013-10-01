package com.pmease.gitop.web.page;

import org.apache.shiro.SecurityUtils;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.base.Optional;
import com.pmease.gitop.core.model.User;

@SuppressWarnings("serial")
public abstract class AbstractLayoutPage extends BasePage {

	public AbstractLayoutPage() {
		commonInit();
	}
	
	public AbstractLayoutPage(PageParameters params) {
		super(params);
		commonInit();
	}
	
	public AbstractLayoutPage(IModel<?> model) {
		super(model);
		commonInit();
	}
	
	private void commonInit() {
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
