package io.onedev.server.web.page.layout;

import com.google.common.collect.Lists;
import io.onedev.server.web.page.builds.BuildListPage;
import io.onedev.server.web.page.issues.IssueListPage;
import io.onedev.server.web.page.project.NewProjectPage;
import io.onedev.server.web.page.project.ProjectListPage;
import io.onedev.server.web.page.pullrequests.PullRequestListPage;
import org.apache.wicket.core.request.handler.PageProvider;

import java.util.ArrayList;
import java.util.List;

public class DefaultMainMenuCustomization implements MainMenuCustomization {

	private static final long serialVersionUID = 1L;

	@Override
	public PageProvider getHomePage(boolean failsafe) {
		return new PageProvider(ProjectListPage.class, ProjectListPage.paramsOf(0, 0));
	}

	@Override
	public List<SidebarMenuItem> getMainMenuItems() {
		List<SidebarMenuItem> menuItems = new ArrayList<>();
		
		menuItems.add(new SidebarMenuItem.Page("project", "Projects", ProjectListPage.class, 
				ProjectListPage.paramsOf(0, 0), Lists.newArrayList(NewProjectPage.class)));
//		menuItems.add(new SidebarMenuItem.Page("code", "Code Search", CodeSearchPage.class, new PageParameters()));
		menuItems.add(new SidebarMenuItem.Page("pull-request", "Pull Requests", PullRequestListPage.class, 
				PullRequestListPage.paramsOf(0)));
		menuItems.add(new SidebarMenuItem.Page("bug", "Issues", IssueListPage.class, 
				IssueListPage.paramsOf(0)));
		menuItems.add(new SidebarMenuItem.Page("play-circle", "Builds", BuildListPage.class, 
				BuildListPage.paramsOf(0, 0)));
		
		return menuItems;
	}

}
