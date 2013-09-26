package com.pmease.gitop.web.page;

import org.apache.shiro.SecurityUtils;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.AbstractReadOnlyModel;

import com.pmease.gitop.core.model.User;
import com.pmease.gitop.web.common.component.avatar.AvatarImage;
import com.pmease.gitop.web.model.UserModel;
import com.pmease.gitop.web.page.account.RegisterPage;
import com.pmease.gitop.web.shiro.LoginPage;
import com.pmease.gitop.web.shiro.LogoutPage;

@SuppressWarnings("serial")
public abstract class AbstractLayoutPage extends BasePage {

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new BookmarkablePageLink<Void>("homeLink", getApplication().getHomePage()));
		add(new BookmarkablePageLink<Void>("loginLink", LoginPage.class).setVisibilityAllowed(!isSignedIn()));
		add(new BookmarkablePageLink<Void>("registerLink", RegisterPage.class).setVisibilityAllowed(!isSignedIn()));
		
		add(new BookmarkablePageLink<Void>("logoutLink", LogoutPage.class).setVisibilityAllowed(isSignedIn()));
		if (User.getCurrent() != null) {
			add(new AvatarImage("useravatar", new UserModel(User.getCurrent())));
			add(new Label("username", new AbstractReadOnlyModel<String>() {

				@Override
				public String getObject() {
					return getCurrentUser().getName();
				}
			}));
		}
	}
	
	protected boolean isSignedIn() {
		return SecurityUtils.getSubject().isAuthenticated();
	}
	
	protected User getCurrentUser() {
		return User.getCurrent();
	}
}
