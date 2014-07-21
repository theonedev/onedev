package com.pmease.gitplex.web.page.layout;

import com.pmease.gitplex.core.GitPlex;

import org.apache.shiro.SecurityUtils;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;

import com.pmease.gitplex.core.manager.UserManager;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.core.permission.ObjectPermission;
import com.pmease.gitplex.web.component.user.UserLink;
import com.pmease.gitplex.web.model.UserModel;
import com.pmease.gitplex.web.page.account.RegisterPage;
import com.pmease.gitplex.web.page.account.setting.profile.AccountProfilePage;
import com.pmease.gitplex.web.page.admin.AdministrationOverviewPage;
import com.pmease.gitplex.web.page.repository.settings.CreateRepositoryPage;
import com.pmease.gitplex.web.shiro.LoginPage;
import com.pmease.gitplex.web.shiro.LogoutPage;

@SuppressWarnings("serial")
public class GlobalHeaderPanel extends Panel {

	public GlobalHeaderPanel(String id) {
		super(id);
	}

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
			
			add(new BookmarkablePageLink<Void>("newRepoLink", CreateRepositoryPage.class));
			add(new BookmarkablePageLink<Void>("adminLink", AdministrationOverviewPage.class) {
				@Override
				protected void onConfigure() {
					setVisible(SecurityUtils.getSubject().isPermitted(ObjectPermission.ofSystemAdmin()));
				}
			});
		} else {
			add(new WebMarkupContainer("userLink").setVisible(false));
			add(new WebMarkupContainer("profileLink").setVisible(false));
			add(new WebMarkupContainer("newRepoLink").setVisible(false));
			add(new WebMarkupContainer("adminLink").setVisible(false));
		}
	}
	
}
