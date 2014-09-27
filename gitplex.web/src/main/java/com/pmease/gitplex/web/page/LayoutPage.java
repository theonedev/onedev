package com.pmease.gitplex.web.page;

import org.apache.shiro.SecurityUtils;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.LoadableDetachableModel;

import com.google.common.base.Optional;
import com.pmease.commons.wicket.behavior.TooltipBehavior;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.UserManager;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.core.permission.ObjectPermission;
import com.pmease.gitplex.web.component.user.UserLink;
import com.pmease.gitplex.web.model.UserModel;
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
		boolean signedIn = currentUser != null;
		
		add(new BookmarkablePageLink<Void>("homeLink", getApplication().getHomePage()));
		
		add(new BookmarkablePageLink<Void>("loginLink", LoginPage.class).setVisible(!signedIn));
		add(new BookmarkablePageLink<Void>("registerLink", RegisterPage.class).setVisible(!signedIn));
		add(new BookmarkablePageLink<Void>("logoutLink", LogoutPage.class).setVisible(signedIn));
		
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
					setVisible(SecurityUtils.getSubject().isPermitted(ObjectPermission.ofSystemAdmin()));
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
				setVisible(SecurityUtils.getSubject().isPermitted(ObjectPermission.ofSystemAdmin()));
			}
			
		}.add(new TooltipBehavior()));
	}
	
	protected Optional<User> currentUser() {
	    return Optional.<User>fromNullable(GitPlex.getInstance(UserManager.class).getCurrent());
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
