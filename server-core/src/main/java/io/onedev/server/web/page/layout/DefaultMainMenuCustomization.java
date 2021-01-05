package io.onedev.server.web.page.layout;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import io.onedev.server.web.page.DashboardPage;
import io.onedev.server.web.page.base.BasePage;
import io.onedev.server.web.page.builds.BuildListPage;
import io.onedev.server.web.page.issues.IssueListPage;
import io.onedev.server.web.page.project.NewProjectPage;
import io.onedev.server.web.page.project.ProjectListPage;
import io.onedev.server.web.page.pullrequests.PullRequestListPage;

public class DefaultMainMenuCustomization implements MainMenuCustomization {

	private static final long serialVersionUID = 1L;

	@Override
	public Class<? extends BasePage> getHomePage() {
		return DashboardPage.class;
	}

	@Override
	public List<SidebarMenuItem> getMainMenuItems() {
		List<SidebarMenuItem> menuItems = new ArrayList<>();
		
		menuItems.add(new SidebarMenuItem.Page("project", "Projects", ProjectListPage.class, 
				ProjectListPage.paramsOf(0, 0), Lists.newArrayList(NewProjectPage.class)));
		menuItems.add(new SidebarMenuItem.Page("pull-request", "Pull Requests", PullRequestListPage.class, 
				PullRequestListPage.paramsOf(0)));
		menuItems.add(new SidebarMenuItem.Page("bug", "Issues", IssueListPage.class, 
				IssueListPage.paramsOf(0)));
		menuItems.add(new SidebarMenuItem.Page("play-circle", "Builds", BuildListPage.class, 
				BuildListPage.paramsOf(0, 0)));
		
		return menuItems;
	}

}
