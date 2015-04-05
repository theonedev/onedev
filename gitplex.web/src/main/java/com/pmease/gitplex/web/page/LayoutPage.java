package com.pmease.gitplex.web.page;

import org.apache.shiro.SecurityUtils;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.LoadableDetachableModel;

import com.pmease.commons.wicket.behavior.TooltipBehavior;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.UserManager;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.core.permission.Permission;
import com.pmease.gitplex.web.component.user.UserLink;
import com.pmease.gitplex.web.model.UserModel;
import com.pmease.gitplex.web.page.account.AccountNotificationsPage;
import com.pmease.gitplex.web.page.account.AccountProfilePage;
import com.pmease.gitplex.web.page.account.RegisterPage;
import com.pmease.gitplex.web.page.account.repository.NewRepositoryPage;
import com.pmease.gitplex.web.page.admin.SystemSettingPage;
import com.pmease.gitplex.web.shiro.LoginPage;
import com.pmease.gitplex.web.shiro.LogoutPage;

@SuppressWarnings("serial")
public abstract class LayoutPage extends BasePage {

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		User currentUser = GitPlex.getInstance(UserManager.class).getCurrent();
		final boolean signedIn = currentUser != null;
		
		add(new BookmarkablePageLink<Void>("homeLink", getApplication().getHomePage()));
		
		add(new BookmarkablePageLink<Void>("loginLink", LoginPage.class).setVisible(!signedIn));
		add(new BookmarkablePageLink<Void>("registerLink", RegisterPage.class).setVisible(!signedIn));
		add(new BookmarkablePageLink<Void>("logoutLink", LogoutPage.class).setVisible(signedIn));
		if (currentUser != null) {
			add(new BookmarkablePageLink<Void>("notificationLink", 
					AccountNotificationsPage.class, 
					AccountNotificationsPage.paramsOf(currentUser)) {
	
				@Override
				protected void onConfigure() {
					super.onConfigure();
					setVisible(!getCurrentUser().getRequestNotifications().isEmpty());
				}
				
			});
		} else {
			add(new WebMarkupContainer("notificationLink").setVisible(false));
		}
		
		if (signedIn) {
			add(new UserLink("userLink", new UserModel(currentUser)));
			add(new BookmarkablePageLink<Void>("profileLink", 
												AccountProfilePage.class, 
												AccountProfilePage.paramsOf(currentUser)));
			
			add(new BookmarkablePageLink<Void>("newRepoLink", NewRepositoryPage.class, 
					NewRepositoryPage.paramsOf(currentUser)));
			add(new BookmarkablePageLink<Void>("adminLink", SystemSettingPage.class) {
				@Override
				protected void onConfigure() {
					setVisible(SecurityUtils.getSubject().isPermitted(Permission.ofSystemAdmin()));
				}
			});
		} else {
			add(new WebMarkupContainer("userLink").setVisible(false));
			add(new WebMarkupContainer("newRepoLink").setVisible(false));
			add(new WebMarkupContainer("adminLink").setVisible(false));
		}
		
		add(new Label("gitError", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				return "Git configuration error: " + GitPlex.getInstance().getGitError();
			}
			
		}) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(GitPlex.getInstance().getGitError() != null);
			}
			
		}.setEscapeModelStrings(false));
		
		add(new Link<Void>("checkGit") {

			@Override
			public void onClick() {
				GitPlex.getInstance().checkGit();
			}
			
		}.add(new TooltipBehavior()));
		
		add(new BookmarkablePageLink<Void>("configureGit", SystemSettingPage.class) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.getSubject().isPermitted(Permission.ofSystemAdmin()));
			}
			
		}.add(new TooltipBehavior()));
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
