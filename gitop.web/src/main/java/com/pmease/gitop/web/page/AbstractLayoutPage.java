package com.pmease.gitop.web.page;

import org.apache.shiro.SecurityUtils;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.LoadableDetachableModel;

import com.google.common.base.Optional;
import com.pmease.commons.wicket.behavior.TooltipBehavior;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.UserManager;
import com.pmease.gitop.model.User;
import com.pmease.gitop.model.permission.ObjectPermission;
import com.pmease.gitop.web.page.admin.AdministrationPage;
import com.pmease.gitop.web.util.WicketUtils;

@SuppressWarnings("serial")
public abstract class AbstractLayoutPage extends BasePage {

	@Override
	protected void onPageInitialize() {
		super.onPageInitialize();
		add(new GlobalHeaderPanel("header"));
		
		add(new Label("gitError", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				return "Git configuration error: " + Gitop.getInstance().getGitError(); 
			}
			
		}) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(Gitop.getInstance().getGitError() != null);
			}
			
		}.setEscapeModelStrings(false));
		
		add(new Link<Void>("checkGit") {

			@Override
			public void onClick() {
				Gitop.getInstance().checkGit();
			}
			
		}.add(new TooltipBehavior()));
		
		add(new BookmarkablePageLink<Void>("configureGit", AdministrationPage.class, WicketUtils.newPageParams("tabId", "system-settings")) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.getSubject().isPermitted(ObjectPermission.ofSystemAdmin()));
			}
			
		}.add(new TooltipBehavior()));
	}
	
	protected Optional<User> currentUser() {
	    return Optional.<User>fromNullable(Gitop.getInstance(UserManager.class).getCurrent());
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
