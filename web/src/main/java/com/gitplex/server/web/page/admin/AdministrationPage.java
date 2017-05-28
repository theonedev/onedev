package com.gitplex.server.web.page.admin;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;

import com.gitplex.server.security.SecurityUtils;
import com.gitplex.server.web.component.tabbable.PageTab;
import com.gitplex.server.web.component.tabbable.Tabbable;
import com.gitplex.server.web.page.admin.group.NewGroupPage;
import com.gitplex.server.web.page.admin.group.GroupListPage;
import com.gitplex.server.web.page.admin.user.NewUserPage;
import com.gitplex.server.web.page.admin.user.UserListPage;
import com.gitplex.server.web.page.layout.LayoutPage;

@SuppressWarnings("serial")
public abstract class AdministrationPage extends LayoutPage {

	@Override
	protected boolean isPermitted() {
		return SecurityUtils.isAdministrator();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		List<PageTab> tabs = new ArrayList<>();
		tabs.add(new AdministrationTab("System Setting", "fa fa-fw fa-sliders", SystemSettingPage.class));
		tabs.add(new AdministrationTab("Security Setting", "fa fa-fw fa-lock", SecuritySettingPage.class));
		tabs.add(new AdministrationTab("Mail Setting", "fa fa-fw fa-envelope", MailSettingPage.class));
		tabs.add(new AdministrationTab("Database Backup", "fa fa-fw fa-database", DatabaseBackupPage.class));
		tabs.add(new AdministrationTab("User Management", "fa fa-fw fa-user", UserListPage.class, NewUserPage.class));
		tabs.add(new AdministrationTab("Group Management", "fa fa-fw fa-group", GroupListPage.class, NewGroupPage.class));
		tabs.add(new AdministrationTab("Server Log", "fa fa-fw fa-file-text-o", ServerLogPage.class));
		tabs.add(new AdministrationTab("Server Information", "fa fa-fw fa-desktop", ServerInformationPage.class));
		
		add(new Tabbable("tabs", tabs));
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new AdministrationResourceReference()));
	}

	@Override
	protected Component newContextHead(String componentId) {
		return new Label(componentId, "Server Administration");
	}

}
