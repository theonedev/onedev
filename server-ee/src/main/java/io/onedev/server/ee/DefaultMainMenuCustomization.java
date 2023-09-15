package io.onedev.server.ee;

import com.google.common.collect.Lists;
import io.onedev.server.ee.dashboard.DashboardPage;
import io.onedev.server.ee.xsearch.FileSearchPage;
import io.onedev.server.ee.xsearch.SymbolSearchPage;
import io.onedev.server.ee.xsearch.TextSearchPage;
import io.onedev.server.web.page.builds.BuildListPage;
import io.onedev.server.web.page.issues.IssueListPage;
import io.onedev.server.web.page.layout.MainMenuCustomization;
import io.onedev.server.web.page.layout.SidebarMenuItem;
import io.onedev.server.web.page.project.NewProjectPage;
import io.onedev.server.web.page.project.ProjectListPage;
import io.onedev.server.web.page.pullrequests.PullRequestListPage;
import io.onedev.server.web.util.WicketUtils;
import org.apache.wicket.core.request.handler.PageProvider;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.util.ArrayList;
import java.util.List;

public class DefaultMainMenuCustomization implements MainMenuCustomization {

	private static final long serialVersionUID = 1L;

	@Override
	public PageProvider getHomePage(boolean failsafe) {
		if (WicketUtils.isSubscriptionActive())
			return new PageProvider(DashboardPage.class, DashboardPage.paramsOf(null, failsafe));
		else
			return new PageProvider(ProjectListPage.class, ProjectListPage.paramsOf(0, 0));
	}

	@Override
	public List<SidebarMenuItem> getMainMenuItems() {
		List<SidebarMenuItem> menuItems = new ArrayList<>();
		
		if (WicketUtils.getPage().isSubscriptionActive()) {
			menuItems.add(new SidebarMenuItem.Page("dashboard", "Dashboards", DashboardPage.class,
					new PageParameters()));
		}

		menuItems.add(new SidebarMenuItem.Page("project", "Projects", ProjectListPage.class,
				ProjectListPage.paramsOf(0, 0), Lists.newArrayList(NewProjectPage.class)));
		menuItems.add(new SidebarMenuItem.Page("pull-request", "Pull Requests", PullRequestListPage.class,
				PullRequestListPage.paramsOf(0)));
		menuItems.add(new SidebarMenuItem.Page("bug", "Issues", IssueListPage.class,
				IssueListPage.paramsOf(0)));
		menuItems.add(new SidebarMenuItem.Page("play-circle", "Builds", BuildListPage.class,
				BuildListPage.paramsOf(0, 0)));
		
		if (WicketUtils.getPage().isSubscriptionActive()) {
			var codeSearchMenuItems = new ArrayList<SidebarMenuItem>();
			codeSearchMenuItems.add(new SidebarMenuItem.Page(null, "Text", TextSearchPage.class, new PageParameters()));
			codeSearchMenuItems.add(new SidebarMenuItem.Page(null, "Files", FileSearchPage.class, new PageParameters()));
			codeSearchMenuItems.add(new SidebarMenuItem.Page(null, "Symbols", SymbolSearchPage.class, new PageParameters()));
			menuItems.add(new SidebarMenuItem.SubMenu("code", "Code Search", codeSearchMenuItems));
		}
		
		return menuItems;
	}

}
