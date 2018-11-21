package io.onedev.server.web.page.admin;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;

import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.component.sidebar.SideBar;
import io.onedev.server.web.component.tabbable.PageTab;
import io.onedev.server.web.component.tabbable.Tab;
import io.onedev.server.web.page.admin.authenticator.AuthenticatorPage;
import io.onedev.server.web.page.admin.databasebackup.DatabaseBackupPage;
import io.onedev.server.web.page.admin.group.GroupListPage;
import io.onedev.server.web.page.admin.group.GroupPage;
import io.onedev.server.web.page.admin.group.NewGroupPage;
import io.onedev.server.web.page.admin.issuesetting.IssueFieldListPage;
import io.onedev.server.web.page.admin.issuesetting.GlobalIssueSettingPage;
import io.onedev.server.web.page.admin.licensemanagement.LicenseManagementPage;
import io.onedev.server.web.page.admin.mailsetting.MailSettingPage;
import io.onedev.server.web.page.admin.securitysetting.SecuritySettingPage;
import io.onedev.server.web.page.admin.serverinformation.ServerInformationPage;
import io.onedev.server.web.page.admin.serverlog.ServerLogPage;
import io.onedev.server.web.page.admin.systemsetting.SystemSettingPage;
import io.onedev.server.web.page.admin.user.NewUserPage;
import io.onedev.server.web.page.admin.user.UserListPage;
import io.onedev.server.web.page.admin.user.UserPage;
import io.onedev.server.web.page.layout.LayoutPage;

@SuppressWarnings("serial")
public abstract class AdministrationPage extends LayoutPage {

	@Override
	protected boolean isPermitted() {
		return SecurityUtils.isAdministrator();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new SideBar("sidebar", "administration.miniSidebar") {

			@Override
			protected List<? extends Tab> newTabs() {
				List<PageTab> tabs = new ArrayList<>();
				tabs.add(new AdministrationTab("User Management", "fa fa-fw fa-user", UserListPage.class, NewUserPage.class, UserPage.class));
				tabs.add(new AdministrationTab("Group Management", "fa fa-fw fa-group", GroupListPage.class, NewGroupPage.class, GroupPage.class));
				tabs.add(new AdministrationTab("System Setting", "fa fa-fw fa-sliders", SystemSettingPage.class));
				tabs.add(new AdministrationTab("Security Setting", "fa fa-fw fa-lock", SecuritySettingPage.class));
				tabs.add(new AdministrationTab("Issue Setting", "fa fa-fw fa-bug", IssueFieldListPage.class, GlobalIssueSettingPage.class));
				tabs.add(new AdministrationTab("External Authentication", "fa fa-fw fa-key", AuthenticatorPage.class));
				tabs.add(new AdministrationTab("Mail Setting", "fa fa-fw fa-envelope", MailSettingPage.class));
				tabs.add(new AdministrationTab("Database Backup", "fa fa-fw fa-database", DatabaseBackupPage.class));
				tabs.add(new AdministrationTab("Server Log", "fa fa-fw fa-file-text-o", ServerLogPage.class));
				tabs.add(new AdministrationTab("Server Information", "fa fa-fw fa-desktop", ServerInformationPage.class));
				tabs.add(new AdministrationTab("License Management", "fa fa-fw fa-vcard-o", LicenseManagementPage.class));
				return tabs;
			}
			
		});
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new AdministrationResourceReference()));
	}

}
