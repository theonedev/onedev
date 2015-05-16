package com.pmease.gitplex.web.page.main;

import java.util.ArrayList;
import java.util.List;

import org.apache.shiro.SecurityUtils;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;

import com.pmease.commons.wicket.component.tabbable.PageTab;
import com.pmease.commons.wicket.component.tabbable.Tabbable;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.core.permission.Permission;
import com.pmease.gitplex.web.component.user.UserLink;
import com.pmease.gitplex.web.model.UserModel;
import com.pmease.gitplex.web.page.account.AccountPage;
import com.pmease.gitplex.web.page.account.list.AccountListPage;
import com.pmease.gitplex.web.page.account.list.NewAccountPage;
import com.pmease.gitplex.web.page.account.notification.AccountNotificationPage;
import com.pmease.gitplex.web.page.account.overview.AccountOverviewPage;
import com.pmease.gitplex.web.page.admin.AdministrationPage;
import com.pmease.gitplex.web.page.admin.SystemSettingPage;
import com.pmease.gitplex.web.page.base.BasePage;
import com.pmease.gitplex.web.page.home.HomePage;
import com.pmease.gitplex.web.page.repository.RepositoryPage;
import com.pmease.gitplex.web.page.repository.list.RepoListPage;
import com.pmease.gitplex.web.page.repository.overview.RepoOverviewPage;
import com.pmease.gitplex.web.page.security.LoginPage;
import com.pmease.gitplex.web.page.security.LogoutPage;
import com.pmease.gitplex.web.page.security.RegisterPage;

@SuppressWarnings("serial")
public abstract class MainPage extends BasePage {

	public MainPage() {
	}
	
	public MainPage(PageParameters params) {
		super(params);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		WebMarkupContainer topNav = new WebMarkupContainer("topNav");
		add(topNav);
		
		topNav.add(new BookmarkablePageLink<Void>("home", HomePage.class));

		WebMarkupContainer accountLink;
		if (this instanceof AccountPage) {
			AccountPage accountPage = (AccountPage) this;
			accountLink = new BookmarkablePageLink<Void>("account", 
					AccountOverviewPage.class, 
					AccountPage.paramsOf(accountPage.getAccount()));
			accountLink.add(new Label("name", accountPage.getAccount().getName()));
		} else {
			accountLink = new WebMarkupContainer("account");
			accountLink.add(new Label("name"));
			accountLink.setVisible(false);
		}
		topNav.add(accountLink);

		WebMarkupContainer repoLink;
		if (this instanceof RepositoryPage) {
			RepositoryPage repoPage = (RepositoryPage) this;
			repoLink = new BookmarkablePageLink<Void>("repository", 
					RepoOverviewPage.class, 
					RepositoryPage.paramsOf(repoPage.getRepository()));
			repoLink.add(new Label("name", repoPage.getRepository().getName()));
		} else {
			repoLink = new WebMarkupContainer("repository");
			repoLink.add(new Label("name"));
			repoLink.setVisible(false);
		}
		topNav.add(repoLink);
		
		add(new Tabbable("mainTabs", newMainTabs()));
		add(newMainActions("mainActions"));
		
		User currentUser = getCurrentUser();
		boolean signedIn = currentUser != null;

		WebMarkupContainer topTray = new WebMarkupContainer("topTray");
		add(topTray);
		
		topTray.add(new BookmarkablePageLink<Void>("login", LoginPage.class).setVisible(!signedIn));
		topTray.add(new BookmarkablePageLink<Void>("register", RegisterPage.class).setVisible(!signedIn));
		topTray.add(new BookmarkablePageLink<Void>("logout", LogoutPage.class).setVisible(signedIn));
		if (currentUser != null) {
			topTray.add(new BookmarkablePageLink<Void>("notification", 
					AccountNotificationPage.class, 
					AccountNotificationPage.paramsOf(currentUser)) {
	
				@Override
				protected void onConfigure() {
					super.onConfigure();
					setVisible(!getCurrentUser().getRequestNotifications().isEmpty());
				}
				
			});
		} else {
			topTray.add(new WebMarkupContainer("notification").setVisible(false));
		}
		
		if (signedIn)
			topTray.add(new UserLink("user", new UserModel(currentUser)));
		else  
			topTray.add(new WebMarkupContainer("user").setVisible(false));
	}
	
	protected List<PageTab> newMainTabs() {
		List<PageTab> tabs = new ArrayList<>();
		tabs.add(new PageTab(Model.of("Home"), HomePage.class));
		
		tabs.add(new PageTab(Model.of("Accounts"), AccountListPage.class, NewAccountPage.class));
		
		tabs.add(new PageTab(Model.of("Repositories"), RepoListPage.class));
		
		if (SecurityUtils.getSubject().isPermitted(Permission.ofSystemAdmin()))
			tabs.add(new PageTab(Model.of("Administration"), SystemSettingPage.class, AdministrationPage.class));
		
		return tabs;
	}
	
	protected WebMarkupContainer newMainActions(String id) {
		return new WebMarkupContainer(id);
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

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(CssHeaderItem.forReference(new CssResourceReference(MainPage.class, "main.css")));
	}
	
}
