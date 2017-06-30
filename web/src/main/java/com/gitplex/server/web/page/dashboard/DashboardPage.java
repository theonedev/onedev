package com.gitplex.server.web.page.dashboard;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;

import com.gitplex.server.GitPlex;
import com.gitplex.server.manager.GroupManager;
import com.gitplex.server.manager.ProjectManager;
import com.gitplex.server.manager.UserManager;
import com.gitplex.server.security.SecurityUtils;
import com.gitplex.server.web.ComponentRenderer;
import com.gitplex.server.web.page.admin.systemsetting.SystemSettingPage;
import com.gitplex.server.web.page.group.GroupListPage;
import com.gitplex.server.web.page.group.NewGroupPage;
import com.gitplex.server.web.page.layout.LayoutPage;
import com.gitplex.server.web.page.project.NewProjectPage;
import com.gitplex.server.web.page.project.ProjectListPage;
import com.gitplex.server.web.page.user.NewUserPage;
import com.gitplex.server.web.page.user.UserListPage;
import com.google.common.collect.Lists;

@SuppressWarnings("serial")
public class DashboardPage extends LayoutPage {

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Link<Void> link = new BookmarkablePageLink<Void>("projects", ProjectListPage.class);
		link.add(new Label("count", GitPlex.getInstance(ProjectManager.class)
				.getAccessibleProjects(getLoginUser()).size()));
		add(link);
		
		add(new BookmarkablePageLink<Void>("addProject", NewProjectPage.class) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.canCreateProjects());
			}
			
		});
		
		link = new BookmarkablePageLink<Void>("users", UserListPage.class);
		link.add(new Label("count", GitPlex.getInstance(UserManager.class).findAll().size()));
		add(link);
		
		add(new BookmarkablePageLink<Void>("addUser", NewUserPage.class) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.isAdministrator());
			}
			
		});
		
		link = new BookmarkablePageLink<Void>("groups", GroupListPage.class);
		link.add(new Label("count", GitPlex.getInstance(GroupManager.class).findAll().size()));
		add(link);
		
		add(new BookmarkablePageLink<Void>("addGroup", NewGroupPage.class) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.isAdministrator());
			}
			
		});
		
		add(new BookmarkablePageLink<Void>("administration", SystemSettingPage.class) {
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.isAdministrator());
			}
			
		});
	}

	@Override
	protected List<ComponentRenderer> getBreadcrumbs() {
		return Lists.newArrayList(new ComponentRenderer() {

			@Override
			public Component render(String componentId) {
				return new Label(componentId, "Home");
			}
			
		});
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new DashboardResourceReference()));
	}

}
