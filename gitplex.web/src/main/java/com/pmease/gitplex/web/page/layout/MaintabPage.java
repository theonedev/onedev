package com.pmease.gitplex.web.page.layout;

import java.util.ArrayList;
import java.util.List;

import org.apache.shiro.SecurityUtils;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.CssResourceReference;

import com.pmease.commons.wicket.component.tabbable.PageTab;
import com.pmease.commons.wicket.component.tabbable.Tabbable;
import com.pmease.gitplex.web.page.HomePage;
import com.pmease.gitplex.web.page.account.AccountPage;
import com.pmease.gitplex.web.page.account.overview.AccountHomePage;
import com.pmease.gitplex.web.page.admin.AdministrationPage;
import com.pmease.gitplex.web.page.admin.SystemSettingPage;
import com.pmease.gitplex.web.page.base.BasePage;
import com.pmease.gitplex.web.page.repository.RepositoryPage;
import com.pmease.gitplex.web.page.repository.home.RepoHomePage;

@SuppressWarnings("serial")
public abstract class MaintabPage extends BasePage {

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new BookmarkablePageLink<Void>("navHome", HomePage.class));

		if (this instanceof AccountPage) {
			AccountPage accountPage = (AccountPage) this;
			add(new BookmarkablePageLink<Void>("navAccount", AccountHomePage.class, 
					AccountPage.paramsOf(accountPage.getAccount())));
		} else {
			add(new WebMarkupContainer("navAccount").setVisible(false));
		}
		
		if (this instanceof RepositoryPage) {
			RepositoryPage repoPage = (RepositoryPage) this;
			add(new BookmarkablePageLink<Void>("navRepo", RepoHomePage.class, 
					RepositoryPage.paramsOf(repoPage.getRepository())));
		} else {
			add(new WebMarkupContainer("navRepo").setVisible(false));
		}
		
		add(new Tabbable("mainTabs", newMainTabs()));
		add(newMainActions("mainActions"));
	}
	
	protected List<PageTab> newMainTabs() {
		List<PageTab> tabs = new ArrayList<>();
		tabs.add(new PageTab(Model.of("Home"), HomePage.class));
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
		
		response.render(CssHeaderItem.forReference(new CssResourceReference(MaintabPage.class, "layout.css")));
	}
	
}
