package io.onedev.server.web;

import org.apache.wicket.core.request.mapper.ResourceMapper;
import org.apache.wicket.markup.html.pages.BrowserInfoPage;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.IRequestMapper;
import org.apache.wicket.request.mapper.CompoundRequestMapper;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.web.asset.icon.IconScope;
import io.onedev.server.web.mapper.BaseResourceMapper;
import io.onedev.server.web.mapper.DynamicPathPageMapper;
import io.onedev.server.web.mapper.DynamicPathResourceMapper;
import io.onedev.server.web.page.admin.authenticator.AuthenticatorPage;
import io.onedev.server.web.page.admin.buildsetting.agent.AgentBuildsPage;
import io.onedev.server.web.page.admin.buildsetting.agent.AgentListPage;
import io.onedev.server.web.page.admin.buildsetting.agent.AgentLogPage;
import io.onedev.server.web.page.admin.buildsetting.agent.AgentOverviewPage;
import io.onedev.server.web.page.admin.buildsetting.jobexecutor.JobExecutorsPage;
import io.onedev.server.web.page.admin.databasebackup.DatabaseBackupPage;
import io.onedev.server.web.page.admin.generalsecuritysetting.GeneralSecuritySettingPage;
import io.onedev.server.web.page.admin.groovyscript.GroovyScriptListPage;
import io.onedev.server.web.page.admin.group.GroupListPage;
import io.onedev.server.web.page.admin.group.authorization.GroupAuthorizationsPage;
import io.onedev.server.web.page.admin.group.create.NewGroupPage;
import io.onedev.server.web.page.admin.group.membership.GroupMembershipsPage;
import io.onedev.server.web.page.admin.group.profile.GroupProfilePage;
import io.onedev.server.web.page.admin.issuesetting.defaultboard.DefaultBoardListPage;
import io.onedev.server.web.page.admin.issuesetting.fieldspec.IssueFieldListPage;
import io.onedev.server.web.page.admin.issuesetting.issuetemplate.IssueTemplateListPage;
import io.onedev.server.web.page.admin.issuesetting.linkspec.LinkSpecListPage;
import io.onedev.server.web.page.admin.issuesetting.statespec.IssueStateListPage;
import io.onedev.server.web.page.admin.issuesetting.transitionspec.StateTransitionListPage;
import io.onedev.server.web.page.admin.mailsetting.MailSettingPage;
import io.onedev.server.web.page.admin.notificationtemplatesetting.IssueNotificationTemplatePage;
import io.onedev.server.web.page.admin.notificationtemplatesetting.PullRequestNotificationTemplatePage;
import io.onedev.server.web.page.admin.performancesetting.PerformanceSettingPage;
import io.onedev.server.web.page.admin.pluginsettings.ContributedAdministrationSettingPage;
import io.onedev.server.web.page.admin.role.NewRolePage;
import io.onedev.server.web.page.admin.role.RoleDetailPage;
import io.onedev.server.web.page.admin.role.RoleListPage;
import io.onedev.server.web.page.admin.serverinformation.ServerInformationPage;
import io.onedev.server.web.page.admin.serverlog.ServerLogPage;
import io.onedev.server.web.page.admin.servicedesk.ServiceDeskSettingPage;
import io.onedev.server.web.page.admin.ssh.SshSettingPage;
import io.onedev.server.web.page.admin.sso.SsoConnectorListPage;
import io.onedev.server.web.page.admin.sso.SsoProcessPage;
import io.onedev.server.web.page.admin.systemsetting.SystemSettingPage;
import io.onedev.server.web.page.admin.user.UserListPage;
import io.onedev.server.web.page.admin.user.accesstoken.UserAccessTokenPage;
import io.onedev.server.web.page.admin.user.authorization.UserAuthorizationsPage;
import io.onedev.server.web.page.admin.user.avatar.UserAvatarPage;
import io.onedev.server.web.page.admin.user.create.NewUserPage;
import io.onedev.server.web.page.admin.user.membership.UserMembershipsPage;
import io.onedev.server.web.page.admin.user.password.UserPasswordPage;
import io.onedev.server.web.page.admin.user.profile.UserProfilePage;
import io.onedev.server.web.page.admin.user.ssh.UserSshKeysPage;
import io.onedev.server.web.page.builds.BuildListPage;
import io.onedev.server.web.page.help.IncompatibilitiesPage;
import io.onedev.server.web.page.help.MethodDetailPage;
import io.onedev.server.web.page.help.ResourceDetailPage;
import io.onedev.server.web.page.help.ResourceListPage;
import io.onedev.server.web.page.issues.IssueListPage;
import io.onedev.server.web.page.my.accesstoken.MyAccessTokenPage;
import io.onedev.server.web.page.my.avatar.MyAvatarPage;
import io.onedev.server.web.page.my.password.MyPasswordPage;
import io.onedev.server.web.page.my.profile.MyProfilePage;
import io.onedev.server.web.page.my.sshkeys.MySshKeysPage;
import io.onedev.server.web.page.project.NewProjectPage;
import io.onedev.server.web.page.project.ProjectListPage;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.page.project.branches.ProjectBranchesPage;
import io.onedev.server.web.page.project.builds.ProjectBuildsPage;
import io.onedev.server.web.page.project.builds.detail.InvalidBuildPage;
import io.onedev.server.web.page.project.builds.detail.artifacts.BuildArtifactsPage;
import io.onedev.server.web.page.project.builds.detail.changes.BuildChangesPage;
import io.onedev.server.web.page.project.builds.detail.dashboard.BuildDashboardPage;
import io.onedev.server.web.page.project.builds.detail.issues.FixedIssuesPage;
import io.onedev.server.web.page.project.builds.detail.log.BuildLogPage;
import io.onedev.server.web.page.project.builds.detail.pipeline.BuildPipelinePage;
import io.onedev.server.web.page.project.children.ProjectChildrenPage;
import io.onedev.server.web.page.project.codecomments.InvalidCodeCommentPage;
import io.onedev.server.web.page.project.codecomments.ProjectCodeCommentsPage;
import io.onedev.server.web.page.project.commits.CommitDetailPage;
import io.onedev.server.web.page.project.commits.ProjectCommitsPage;
import io.onedev.server.web.page.project.compare.RevisionComparePage;
import io.onedev.server.web.page.project.dashboard.ProjectDashboardPage;
import io.onedev.server.web.page.project.imports.ProjectImportPage;
import io.onedev.server.web.page.project.issues.boards.IssueBoardsPage;
import io.onedev.server.web.page.project.issues.create.NewIssuePage;
import io.onedev.server.web.page.project.issues.detail.IssueActivitiesPage;
import io.onedev.server.web.page.project.issues.detail.IssueBuildsPage;
import io.onedev.server.web.page.project.issues.detail.IssueCommitsPage;
import io.onedev.server.web.page.project.issues.detail.IssuePullRequestsPage;
import io.onedev.server.web.page.project.issues.imports.IssueImportPage;
import io.onedev.server.web.page.project.issues.list.ProjectIssueListPage;
import io.onedev.server.web.page.project.issues.milestones.MilestoneBurndownPage;
import io.onedev.server.web.page.project.issues.milestones.MilestoneEditPage;
import io.onedev.server.web.page.project.issues.milestones.MilestoneIssuesPage;
import io.onedev.server.web.page.project.issues.milestones.MilestoneListPage;
import io.onedev.server.web.page.project.issues.milestones.NewMilestonePage;
import io.onedev.server.web.page.project.pullrequests.InvalidPullRequestPage;
import io.onedev.server.web.page.project.pullrequests.ProjectPullRequestsPage;
import io.onedev.server.web.page.project.pullrequests.create.NewPullRequestPage;
import io.onedev.server.web.page.project.pullrequests.detail.activities.PullRequestActivitiesPage;
import io.onedev.server.web.page.project.pullrequests.detail.changes.PullRequestChangesPage;
import io.onedev.server.web.page.project.pullrequests.detail.codecomments.PullRequestCodeCommentsPage;
import io.onedev.server.web.page.project.setting.authorization.ProjectAuthorizationsPage;
import io.onedev.server.web.page.project.setting.avatar.AvatarEditPage;
import io.onedev.server.web.page.project.setting.branchprotection.BranchProtectionsPage;
import io.onedev.server.web.page.project.setting.build.ActionAuthorizationsPage;
import io.onedev.server.web.page.project.setting.build.BuildPreservationsPage;
import io.onedev.server.web.page.project.setting.build.DefaultFixedIssueFiltersPage;
import io.onedev.server.web.page.project.setting.build.JobSecretsPage;
import io.onedev.server.web.page.project.setting.general.GeneralProjectSettingPage;
import io.onedev.server.web.page.project.setting.pluginsettings.ContributedProjectSettingPage;
import io.onedev.server.web.page.project.setting.servicedesk.ProjectServiceDeskSettingPage;
import io.onedev.server.web.page.project.setting.tagprotection.TagProtectionsPage;
import io.onedev.server.web.page.project.setting.webhook.WebHooksPage;
import io.onedev.server.web.page.project.stats.ProjectContribsPage;
import io.onedev.server.web.page.project.stats.SourceLinesPage;
import io.onedev.server.web.page.project.tags.ProjectTagsPage;
import io.onedev.server.web.page.pullrequests.PullRequestListPage;
import io.onedev.server.web.page.simple.error.MethodNotAllowedErrorPage;
import io.onedev.server.web.page.simple.error.PageNotFoundErrorPage;
import io.onedev.server.web.page.simple.security.LoginPage;
import io.onedev.server.web.page.simple.security.LogoutPage;
import io.onedev.server.web.page.simple.security.PasswordResetPage;
import io.onedev.server.web.page.simple.security.SignUpPage;
import io.onedev.server.web.page.simple.serverinit.ServerInitPage;
import io.onedev.server.web.resource.AgentLibResourceReference;
import io.onedev.server.web.resource.AgentLogResourceReference;
import io.onedev.server.web.resource.AgentResourceReference;
import io.onedev.server.web.resource.ArchiveResourceReference;
import io.onedev.server.web.resource.ArtifactResourceReference;
import io.onedev.server.web.resource.AttachmentResourceReference;
import io.onedev.server.web.resource.BuildLogResourceReference;
import io.onedev.server.web.resource.RawBlobResourceReference;
import io.onedev.server.web.resource.ServerLogResourceReference;
import io.onedev.server.web.resource.SvgSpriteResourceReference;

public class BaseUrlMapper extends CompoundRequestMapper {

	@Override
	public CompoundRequestMapper add(IRequestMapper mapper) {
		if (mapper instanceof ResourceMapper && !(mapper instanceof BaseResourceMapper))
			throw new ExplicitException("Base resource mapper should be used");
		return super.add(mapper);
	}

	public BaseUrlMapper(WebApplication app) {
		add(new DynamicPathPageMapper("init", ServerInitPage.class));
		add(new DynamicPathPageMapper("loading", BrowserInfoPage.class));
		add(new DynamicPathPageMapper("issues", IssueListPage.class));
		add(new DynamicPathPageMapper("pull-requests", PullRequestListPage.class));
		add(new DynamicPathPageMapper("builds", BuildListPage.class));
		addProjectPages();
		addMyPages();
		addAdministrationPages();
		addSecurityPages();
		addResources();
		addHelpPages();
		addErrorPages();
	}

	private void addHelpPages() {
		add(new DynamicPathPageMapper("help/incompatibilities", IncompatibilitiesPage.class));
		add(new DynamicPathPageMapper("help/api", ResourceListPage.class));
		add(new DynamicPathPageMapper("help/api/${resource}", ResourceDetailPage.class));
		add(new DynamicPathPageMapper("help/api/${resource}/${method}", MethodDetailPage.class));
	}
	
	private void addMyPages() {
		add(new DynamicPathPageMapper("my/profile", MyProfilePage.class));
		add(new DynamicPathPageMapper("my/avatar", MyAvatarPage.class));
		add(new DynamicPathPageMapper("my/password", MyPasswordPage.class));
		add(new DynamicPathPageMapper("my/ssh-keys", MySshKeysPage.class));
		add(new DynamicPathPageMapper("my/access-token", MyAccessTokenPage.class));
	}

	private void addResources() {
		add(new BaseResourceMapper("downloads/server-log", new ServerLogResourceReference()));
		add(new BaseResourceMapper("downloads/agent-log/${agent}", new AgentLogResourceReference()));
		add(new BaseResourceMapper("downloads/agent.zip", new AgentResourceReference()));
		add(new BaseResourceMapper("downloads/agent-lib", new AgentLibResourceReference()));
		add(new BaseResourceMapper("downloads/projects/${project}/builds/${build}/log", 
				new BuildLogResourceReference()));
		add(new BaseResourceMapper("projects/${project}/archive/${revision}", 
				new ArchiveResourceReference()));
		add(new DynamicPathResourceMapper("projects/${project}/raw/${revision}/${path}", 
				new RawBlobResourceReference()));
		
		// Change AttachmentResource.authorizeGroup accordingly if change attachment url here
		add(new BaseResourceMapper("projects/${project}/attachment/${group}/${attachment}", 
				new AttachmentResourceReference()));
		
		add(new DynamicPathResourceMapper("downloads/projects/${project}/builds/${build}/artifacts/${path}", 
				new ArtifactResourceReference()));
		add(new BaseResourceMapper(SvgSpriteResourceReference.DEFAULT_MOUNT_PATH, 
				new SvgSpriteResourceReference(IconScope.class)));
	}
	
	private void addErrorPages() {
		add(new DynamicPathPageMapper("/errors/404", PageNotFoundErrorPage.class));
		add(new DynamicPathPageMapper("/errors/405", MethodNotAllowedErrorPage.class));
	}
	
	private void addSecurityPages() {
		add(new DynamicPathPageMapper("login", LoginPage.class));
		add(new DynamicPathPageMapper("logout", LogoutPage.class));
		add(new DynamicPathPageMapper("signup", SignUpPage.class));
		add(new DynamicPathPageMapper("reset-password", PasswordResetPage.class));
		add(new DynamicPathPageMapper(SsoProcessPage.MOUNT_PATH + "/${stage}/${connector}", SsoProcessPage.class));
	}
 	
	private void addAdministrationPages() {
		add(new DynamicPathPageMapper("administration", UserListPage.class));
		add(new DynamicPathPageMapper("administration/users", UserListPage.class));
		add(new DynamicPathPageMapper("administration/users/new", NewUserPage.class));
		add(new DynamicPathPageMapper("administration/users/${user}/profile", UserProfilePage.class));
		add(new DynamicPathPageMapper("administration/users/${user}/groups", UserMembershipsPage.class));
		add(new DynamicPathPageMapper("administration/users/${user}/authorizations", UserAuthorizationsPage.class));
		add(new DynamicPathPageMapper("administration/users/${user}/avatar", UserAvatarPage.class));
		add(new DynamicPathPageMapper("administration/users/${user}/password", UserPasswordPage.class));
		add(new DynamicPathPageMapper("administration/users/${user}/ssh-keys", UserSshKeysPage.class));
		add(new DynamicPathPageMapper("administration/users/${user}/access-token", UserAccessTokenPage.class));
		
		add(new DynamicPathPageMapper("administration/roles", RoleListPage.class));
		add(new DynamicPathPageMapper("administration/roles/new", NewRolePage.class));
		add(new DynamicPathPageMapper("administration/roles/${role}", RoleDetailPage.class));
		
		add(new DynamicPathPageMapper("administration/groups", GroupListPage.class));
		add(new DynamicPathPageMapper("administration/groups/new", NewGroupPage.class));
		add(new DynamicPathPageMapper("administration/groups/${group}/profile", GroupProfilePage.class));
		add(new DynamicPathPageMapper("administration/groups/${group}/members", GroupMembershipsPage.class));
		add(new DynamicPathPageMapper("administration/groups/${group}/authorizations", GroupAuthorizationsPage.class));
		
		add(new DynamicPathPageMapper("administration/settings/system", SystemSettingPage.class));
		add(new DynamicPathPageMapper("administration/settings/mail", MailSettingPage.class));
		add(new DynamicPathPageMapper("administration/settings/service-desk-setting", 
				ServiceDeskSettingPage.class));
		add(new DynamicPathPageMapper("administration/settings/issue-notification-template", 
				IssueNotificationTemplatePage.class));
		add(new DynamicPathPageMapper("administration/settings/pull-request-notification-template", 
				PullRequestNotificationTemplatePage.class));
		add(new DynamicPathPageMapper("administration/settings/performance", PerformanceSettingPage.class));
		add(new DynamicPathPageMapper("administration/settings/backup", DatabaseBackupPage.class));
		add(new DynamicPathPageMapper("administration/settings/security", GeneralSecuritySettingPage.class));
		add(new DynamicPathPageMapper("administration/settings/authenticator", AuthenticatorPage.class));
		add(new DynamicPathPageMapper("administration/settings/sso-connectors", SsoConnectorListPage.class));
		add(new DynamicPathPageMapper("administration/settings/ssh", SshSettingPage.class));

		add(new DynamicPathPageMapper("administration/agents", AgentListPage.class));
		add(new DynamicPathPageMapper("administration/agents/${agent}/overview", AgentOverviewPage.class));
		add(new DynamicPathPageMapper("administration/agents/${agent}/builds", AgentBuildsPage.class));
		add(new DynamicPathPageMapper("administration/agents/${agent}/log", AgentLogPage.class));
		add(new DynamicPathPageMapper("administration/settings/job-executors", JobExecutorsPage.class));
		add(new DynamicPathPageMapper("administration/settings/groovy-scripts", GroovyScriptListPage.class));
		
		add(new DynamicPathPageMapper("administration/settings/issue-fields", IssueFieldListPage.class));
		add(new DynamicPathPageMapper("administration/settings/issue-states", IssueStateListPage.class));
		add(new DynamicPathPageMapper("administration/settings/state-transitions", StateTransitionListPage.class));
		add(new DynamicPathPageMapper("administration/settings/issue-boards", DefaultBoardListPage.class));
		add(new DynamicPathPageMapper("administration/settings/issue-links", LinkSpecListPage.class));
		add(new DynamicPathPageMapper("administration/settings/issue-templates", IssueTemplateListPage.class));
		
		add(new DynamicPathPageMapper("administration/settings/${setting}", ContributedAdministrationSettingPage.class));
		
		add(new DynamicPathPageMapper("administration/server-log", ServerLogPage.class));
		add(new DynamicPathPageMapper("administration/server-information", ServerInformationPage.class));
	}
	
	private void addProjectPages() {
		add(new DynamicPathPageMapper("projects", ProjectListPage.class));
		add(new DynamicPathPageMapper("projects/new", NewProjectPage.class));
		add(new DynamicPathPageMapper("projects/import/${importer}", ProjectImportPage.class));
		add(new DynamicPathPageMapper("projects/${project}", ProjectDashboardPage.class));

		add(new DynamicPathPageMapper("projects/${project}/blob/#{revision}/#{path}", ProjectBlobPage.class));
		add(new DynamicPathPageMapper("projects/${project}/commits", ProjectCommitsPage.class));
		add(new DynamicPathPageMapper("projects/${project}/commits/${revision}", CommitDetailPage.class));
		add(new DynamicPathPageMapper("projects/${project}/compare", RevisionComparePage.class));
		add(new DynamicPathPageMapper("projects/${project}/stats/contribs", ProjectContribsPage.class));
		add(new DynamicPathPageMapper("projects/${project}/stats/lines", SourceLinesPage.class));

		add(new DynamicPathPageMapper("projects/${project}/branches", ProjectBranchesPage.class));
		add(new DynamicPathPageMapper("projects/${project}/tags", ProjectTagsPage.class));
		add(new DynamicPathPageMapper("projects/${project}/code-comments", ProjectCodeCommentsPage.class));
		add(new DynamicPathPageMapper("projects/${project}/code-comments/${code-comment}/invalid", InvalidCodeCommentPage.class));

		add(new DynamicPathPageMapper("projects/${project}/pulls", ProjectPullRequestsPage.class));
		add(new DynamicPathPageMapper("projects/${project}/pulls/new", NewPullRequestPage.class));
		add(new DynamicPathPageMapper("projects/${project}/pulls/${request}", PullRequestActivitiesPage.class));
		add(new DynamicPathPageMapper("projects/${project}/pulls/${request}/code-comments", PullRequestCodeCommentsPage.class));
		add(new DynamicPathPageMapper("projects/${project}/pulls/${request}/changes", PullRequestChangesPage.class));
		add(new DynamicPathPageMapper("projects/${project}/pulls/${request}/invalid", InvalidPullRequestPage.class));

		add(new DynamicPathPageMapper("projects/${project}/boards", IssueBoardsPage.class));
		add(new DynamicPathPageMapper("projects/${project}/boards/${board}", IssueBoardsPage.class));
		add(new DynamicPathPageMapper("projects/${project}/issues", ProjectIssueListPage.class));
		add(new DynamicPathPageMapper("projects/${project}/issues/${issue}", IssueActivitiesPage.class));
		add(new DynamicPathPageMapper("projects/${project}/issues/${issue}/commits", IssueCommitsPage.class));
		add(new DynamicPathPageMapper("projects/${project}/issues/${issue}/pull-requests", IssuePullRequestsPage.class));
		add(new DynamicPathPageMapper("projects/${project}/issues/${issue}/builds", IssueBuildsPage.class));
		add(new DynamicPathPageMapper("projects/${project}/issues/new", NewIssuePage.class));
		add(new DynamicPathPageMapper("projects/${project}/issues/import/${importer}", IssueImportPage.class));
		add(new DynamicPathPageMapper("projects/${project}/milestones", MilestoneListPage.class));
		add(new DynamicPathPageMapper("projects/${project}/milestones/${milestone}/issues", MilestoneIssuesPage.class));
		add(new DynamicPathPageMapper("projects/${project}/milestones/${milestone}/burndown", MilestoneBurndownPage.class));
		add(new DynamicPathPageMapper("projects/${project}/milestones/${milestone}/edit", MilestoneEditPage.class));
		add(new DynamicPathPageMapper("projects/${project}/milestones/new", NewMilestonePage.class));
		
		add(new DynamicPathPageMapper("projects/${project}/builds", ProjectBuildsPage.class));
		add(new DynamicPathPageMapper("projects/${project}/builds/${build}", BuildDashboardPage.class));
		add(new DynamicPathPageMapper("projects/${project}/builds/${build}/pipeline", BuildPipelinePage.class));
		add(new DynamicPathPageMapper("projects/${project}/builds/${build}/log", BuildLogPage.class));
		add(new DynamicPathPageMapper("projects/${project}/builds/${build}/changes", BuildChangesPage.class));
		add(new DynamicPathPageMapper("projects/${project}/builds/${build}/fixed-issues", FixedIssuesPage.class));
		add(new DynamicPathPageMapper("projects/${project}/builds/${build}/artifacts", BuildArtifactsPage.class));
		add(new DynamicPathPageMapper("projects/${project}/builds/${build}/invalid", InvalidBuildPage.class));
		
		add(new DynamicPathPageMapper("projects/${project}/children", ProjectChildrenPage.class));
		
		add(new DynamicPathPageMapper("projects/${project}/settings/general", GeneralProjectSettingPage.class));
		add(new DynamicPathPageMapper("projects/${project}/settings/authorizations", ProjectAuthorizationsPage.class));
		add(new DynamicPathPageMapper("projects/${project}/settings/avatar-edit", AvatarEditPage.class));
		add(new DynamicPathPageMapper("projects/${project}/settings/branch-protection", BranchProtectionsPage.class));
		add(new DynamicPathPageMapper("projects/${project}/settings/tag-protection", TagProtectionsPage.class));
		add(new DynamicPathPageMapper("projects/${project}/settings/build/job-secrets", JobSecretsPage.class));
		add(new DynamicPathPageMapper("projects/${project}/settings/build/action-authorizations", ActionAuthorizationsPage.class));
		add(new DynamicPathPageMapper("projects/${project}/settings/build/build-preserve-rules", BuildPreservationsPage.class));
		add(new DynamicPathPageMapper("projects/${project}/settings/build/default-fixed-issues-filter", DefaultFixedIssueFiltersPage.class));
		add(new DynamicPathPageMapper("projects/${project}/settings/service-desk", ProjectServiceDeskSettingPage.class));
		add(new DynamicPathPageMapper("projects/${project}/settings/web-hooks", WebHooksPage.class));
		add(new DynamicPathPageMapper("projects/${project}/settings/${setting}", ContributedProjectSettingPage.class));
	}

}
