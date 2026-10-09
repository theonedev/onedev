package io.onedev.server.web.page.project.setting;

import static io.onedev.server.web.translation.Translation._T;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.google.common.collect.Lists;

import io.onedev.server.OneDev;
import io.onedev.server.model.Project;
import io.onedev.server.service.SettingService;
import io.onedev.server.web.editable.EditableUtils;
import io.onedev.server.web.page.layout.SidebarMenu;
import io.onedev.server.web.page.layout.SidebarMenuItem;
import io.onedev.server.web.page.project.setting.ai.ProjectAiSettingPage;
import io.onedev.server.web.page.project.setting.authorization.GroupAuthorizationsPage;
import io.onedev.server.web.page.project.setting.authorization.UserAuthorizationsPage;
import io.onedev.server.web.page.project.setting.avatar.AvatarEditPage;
import io.onedev.server.web.page.project.setting.build.BuildPreservationsPage;
import io.onedev.server.web.page.project.setting.build.DefaultFixedIssueFiltersPage;
import io.onedev.server.web.page.project.setting.build.JobPropertiesPage;
import io.onedev.server.web.page.project.setting.build.JobSecretsPage;
import io.onedev.server.web.page.project.setting.cache.CacheManagementPage;
import io.onedev.server.web.page.project.setting.code.analysis.CodeIndexingSettingPage;
import io.onedev.server.web.page.project.setting.code.branchprotection.BranchProtectionsPage;
import io.onedev.server.web.page.project.setting.code.git.GitPackConfigPage;
import io.onedev.server.web.page.project.setting.code.pullrequest.PullRequestSettingPage;
import io.onedev.server.web.page.project.setting.code.tagprotection.TagProtectionsPage;
import io.onedev.server.web.page.project.setting.general.GeneralProjectSettingPage;
import io.onedev.server.web.page.project.setting.issuesetting.IssueBranchPrefixPage;
import io.onedev.server.web.page.project.setting.pluginsettings.ContributedProjectSettingPage;
import io.onedev.server.web.page.project.setting.servicedesk.ServiceDeskSettingPage;
import io.onedev.server.web.page.project.setting.webhook.WebHooksPage;
import io.onedev.server.web.page.project.setting.wiki.WikiSettingPage;
import io.onedev.server.web.page.project.setting.workspacespec.WorkspaceSpecsPage;

public class ProjectSettingMenu {

	public static List<SidebarMenuItem> getMenuItems(Project project) {
		boolean defaults = Project.DEFAULT_ID.equals(project.getId());
		List<SidebarMenuItem> settingMenuItems = new ArrayList<>();
		settingMenuItems.add(new SidebarMenuItem.Page(null, _T("General"),
				GeneralProjectSettingPage.class, GeneralProjectSettingPage.paramsOf(project)));
		if (!defaults) {
			settingMenuItems.add(new SidebarMenuItem.Page(null, _T("Edit Avatar"),
					AvatarEditPage.class, AvatarEditPage.paramsOf(project)));
		}

		List<SidebarMenuItem> authorizationMenuItems = new ArrayList<>();
		authorizationMenuItems.add(new SidebarMenuItem.Page(null, _T("By User"),
				UserAuthorizationsPage.class, UserAuthorizationsPage.paramsOf(project)));
		authorizationMenuItems.add(new SidebarMenuItem.Page(null, _T("By Group"),
				GroupAuthorizationsPage.class, GroupAuthorizationsPage.paramsOf(project)));
		settingMenuItems.add(new SidebarMenuItem.SubMenu(null, _T("Authorization"), authorizationMenuItems));

		List<SidebarMenuItem> codeSettingMenuItems = new ArrayList<>();
		codeSettingMenuItems.add(new SidebarMenuItem.Page(null, _T("Branch Protection"), 
				BranchProtectionsPage.class, BranchProtectionsPage.paramsOf(project)));
		codeSettingMenuItems.add(new SidebarMenuItem.Page(null, _T("Tag Protection"), 
				TagProtectionsPage.class, TagProtectionsPage.paramsOf(project)));
		codeSettingMenuItems.add(new SidebarMenuItem.Page(null, _T("Code Indexing"), 
				CodeIndexingSettingPage.class, CodeIndexingSettingPage.paramsOf(project)));
		if (!defaults && project.isCodeManagement()) {
			codeSettingMenuItems.add(new SidebarMenuItem.Page(null, _T("Git Pack Config"),
					GitPackConfigPage.class, GitPackConfigPage.paramsOf(project)));
		}
		
		settingMenuItems.add(new SidebarMenuItem.SubMenu(null, _T("Code"), codeSettingMenuItems));
		settingMenuItems.add(new SidebarMenuItem.Page(null, _T("Pull Request"),
				PullRequestSettingPage.class, PullRequestSettingPage.paramsOf(project)));

		if (defaults || project.isIssueManagement()) {
			List<SidebarMenuItem> issueSettingMenuItems = new ArrayList<>();
			issueSettingMenuItems.add(new SidebarMenuItem.Page(null, _T("Branch Prefix"),
					IssueBranchPrefixPage.class, IssueBranchPrefixPage.paramsOf(project)));
			settingMenuItems.add(new SidebarMenuItem.SubMenu(null, _T("Issue"), issueSettingMenuItems));
		}
				
		List<SidebarMenuItem> buildSettingMenuItems = new ArrayList<>();
		
		buildSettingMenuItems.add(new SidebarMenuItem.Page(null, _T("Job Secrets"), 
				JobSecretsPage.class, JobSecretsPage.paramsOf(project)));
		buildSettingMenuItems.add(new SidebarMenuItem.Page(null, _T("Job Properties"),
				JobPropertiesPage.class, JobPropertiesPage.paramsOf(project)));
		buildSettingMenuItems.add(new SidebarMenuItem.Page(null, _T("Build Preserve Rules"), 
				BuildPreservationsPage.class, BuildPreservationsPage.paramsOf(project)));
		buildSettingMenuItems.add(new SidebarMenuItem.Page(null, _T("Default Fixed Issue Filters"), 
				DefaultFixedIssueFiltersPage.class, DefaultFixedIssueFiltersPage.paramsOf(project)));
		
		settingMenuItems.add(new SidebarMenuItem.SubMenu(null, _T("Build"), buildSettingMenuItems));
		settingMenuItems.add(new SidebarMenuItem.Page(null, _T("Workspace Specs"),
				WorkspaceSpecsPage.class, WorkspaceSpecsPage.paramsOf(project)));

		settingMenuItems.add(new SidebarMenuItem.Page(null, _T("Cache Management"),
				CacheManagementPage.class, CacheManagementPage.paramsOf(project)));

		settingMenuItems.add(new SidebarMenuItem.Page(null, _T("Wiki"),
				WikiSettingPage.class, WikiSettingPage.paramsOf(project)));

		if (!defaults && OneDev.getInstance(SettingService.class).getServiceDeskSetting() != null && project.isIssueManagement()) {
			settingMenuItems.add(new SidebarMenuItem.Page(null, _T("Service Desk"), 
					ServiceDeskSettingPage.class, ServiceDeskSettingPage.paramsOf(project)));
		}
		
		SidebarMenuItem webHooksItem = new SidebarMenuItem.Page(null, _T("Web Hooks"), 
				WebHooksPage.class, WebHooksPage.paramsOf(project));			
		settingMenuItems.add(new SidebarMenuItem.SubMenu(null, _T("Notification"), Lists.newArrayList(webHooksItem)));	

		settingMenuItems.add(new SidebarMenuItem.Page(null, _T("AI"),
				ProjectAiSettingPage.class, ProjectAiSettingPage.paramsOf(project)));

		var menu = new SidebarMenu(null, settingMenuItems);
		List<Class<? extends ContributedProjectSetting>> contributedSettingClasses = new ArrayList<>();
		for (ProjectSettingContribution contribution:OneDev.getExtensions(ProjectSettingContribution.class)) {
			for (Class<? extends ContributedProjectSetting> settingClass: contribution.getSettingClasses()) 
				contributedSettingClasses.add(settingClass);
		}
		contributedSettingClasses.sort(Comparator.comparingInt(EditableUtils::getOrder));
					
		for (var contributedSettingClass: contributedSettingClasses) {
			var menuItem = new SidebarMenuItem.Page(
					null,
					_T(EditableUtils.getDisplayName(contributedSettingClass)),
					ContributedProjectSettingPage.class,
					ContributedProjectSettingPage.paramsOf(project, contributedSettingClass));
			var group = EditableUtils.getGroup(contributedSettingClass);
			if (group != null)
				menu.insertMenuItem(new SidebarMenuItem.SubMenu(null, _T(group), Lists.newArrayList(menuItem)));
			else
				menu.insertMenuItem(menuItem);
		}
		return menu.getMenuItems();
	}
}
