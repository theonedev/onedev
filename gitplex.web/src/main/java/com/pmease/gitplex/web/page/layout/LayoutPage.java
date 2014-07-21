package com.pmease.gitplex.web.page.layout;

import com.pmease.gitplex.core.GitPlex;

import org.apache.shiro.SecurityUtils;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.LoadableDetachableModel;

import com.google.common.base.Optional;
import com.pmease.commons.wicket.behavior.TooltipBehavior;
import com.pmease.gitplex.core.manager.UserManager;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.core.permission.ObjectPermission;
import com.pmease.gitplex.web.page.BasePage;
import com.pmease.gitplex.web.page.admin.SystemSettingEdit;

@SuppressWarnings("serial")
public abstract class LayoutPage extends BasePage {

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new GlobalHeaderPanel("header"));
		
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
		
		add(new BookmarkablePageLink<Void>("configureGit", SystemSettingEdit.class) {

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
