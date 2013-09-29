package com.pmease.gitop.web.page;

import org.apache.shiro.SecurityUtils;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;

import com.google.common.base.Optional;
import com.pmease.gitop.core.model.User;
import com.pmease.gitop.web.common.component.link.UserAvatarLink;
import com.pmease.gitop.web.model.UserModel;
import com.pmease.gitop.web.page.account.RegisterPage;
import com.pmease.gitop.web.page.account.setting.profile.AccountProfilePage;
import com.pmease.gitop.web.shiro.LoginPage;
import com.pmease.gitop.web.shiro.LogoutPage;

@SuppressWarnings("serial")
public class GlobalHeaderPanel extends Panel {

	public GlobalHeaderPanel(String id) {
		super(id);
		
		add(new BookmarkablePageLink<Void>("homeLink", getApplication().getHomePage()));

		add(new BookmarkablePageLink<Void>("loginLink", LoginPage.class).setVisibilityAllowed(!isSignedIn()));
		add(new BookmarkablePageLink<Void>("registerLink", RegisterPage.class).setVisibilityAllowed(!isSignedIn()));
		add(new BookmarkablePageLink<Void>("logoutLink", LogoutPage.class).setVisibilityAllowed(isSignedIn()));
		
		if (isSignedIn()) {
			add(new UserAvatarLink("userlink", new UserModel(currentUser().get())));
			add(new BookmarkablePageLink<Void>("profileLink", AccountProfilePage.class));
		} else {
			add(new WebMarkupContainer("userlink").setVisibilityAllowed(false));
		}
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
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
