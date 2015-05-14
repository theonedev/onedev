package com.pmease.gitplex.web.page.main;

import java.util.ArrayList;
import java.util.List;

import org.apache.shiro.SecurityUtils;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;

import com.pmease.commons.wicket.component.tabbable.PageTab;
import com.pmease.commons.wicket.component.tabbable.Tabbable;
import com.pmease.gitplex.web.page.account.AccountPage;
import com.pmease.gitplex.web.page.account.list.AccountListPage;
import com.pmease.gitplex.web.page.account.list.NewAccountPage;
import com.pmease.gitplex.web.page.account.overview.AccountOverviewPage;
import com.pmease.gitplex.web.page.admin.AdministrationPage;
import com.pmease.gitplex.web.page.admin.SystemSettingPage;
import com.pmease.gitplex.web.page.base.BasePage;
import com.pmease.gitplex.web.page.home.HomePage;
import com.pmease.gitplex.web.page.repository.RepositoryPage;
import com.pmease.gitplex.web.page.repository.list.RepoListPage;
import com.pmease.gitplex.web.page.repository.overview.RepoOverviewPage;

@SuppressWarnings("serial")
public abstract class MainPage extends BasePage {

	public MainPage() {
	}
	
	public MainPage(IModel<?> model) {
		super(model);
	}
	
	public MainPage(PageParameters params) {
		super(params);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new BookmarkablePageLink<Void>("navHome", HomePage.class));

		WebMarkupContainer accountLink;
		if (this instanceof AccountPage) {
			AccountPage accountPage = (AccountPage) this;
			accountLink = new BookmarkablePageLink<Void>("navAccount", 
					AccountOverviewPage.class, 
					AccountPage.paramsOf(accountPage.getAccount()));
			accountLink.add(new Label("name", accountPage.getAccount().getName()));
		} else {
			accountLink = new WebMarkupContainer("navAccount");
			accountLink.add(new Label("name"));
			accountLink.setVisible(false);
		}
		add(accountLink);

		WebMarkupContainer repoLink;
		if (this instanceof RepositoryPage) {
			RepositoryPage repoPage = (RepositoryPage) this;
			repoLink = new BookmarkablePageLink<Void>("navRepo", 
					RepoOverviewPage.class, 
					RepositoryPage.paramsOf(repoPage.getRepository()));
			repoLink.add(new Label("name", repoPage.getRepository().getName()));
		} else {
			repoLink = new WebMarkupContainer("navRepo");
			repoLink.add(new Label("name"));
			repoLink.setVisible(false);
		}
		add(repoLink);
		
		add(new Tabbable("mainTabs", newMainTabs()));
		add(newMainActions("mainActions"));
	}
	
	protected List<PageTab> newMainTabs() {
		List<PageTab> tabs = new ArrayList<>();
		tabs.add(new PageTab(Model.of("Home"), HomePage.class));
		
		tabs.add(new PageTab(Model.of("Accounts"), AccountListPage.class, NewAccountPage.class));
		
		tabs.add(new PageTab(Model.of("Repositories"), RepoListPage.class));
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
