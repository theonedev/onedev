package io.onedev.server.web.mapper;

import org.apache.wicket.core.request.mapper.ResourceMapper;
import org.apache.wicket.markup.html.pages.BrowserInfoPage;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.IRequestMapper;
import org.apache.wicket.request.mapper.CompoundRequestMapper;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.web.asset.icon.IconScope;
import io.onedev.server.web.page.admin.authenticator.AuthenticatorPage;
import io.onedev.server.web.page.admin.brandingsetting.BrandingSettingPage;
import io.onedev.server.web.page.admin.buildsetting.agent.AgentBuildsPage;
import io.onedev.server.web.page.admin.buildsetting.agent.AgentListPage;
import io.onedev.server.web.page.admin.buildsetting.agent.AgentLogPage;
import io.onedev.server.web.page.admin.buildsetting.agent.AgentOverviewPage;
import io.onedev.server.web.page.admin.buildsetting.jobexecutor.JobExecutorsPage;
import io.onedev.server.web.page.admin.databasebackup.DatabaseBackupPage;
import io.onedev.server.web.page.admin.gpgsigningkey.GpgSigningKeyPage;
import io.onedev.server.web.page.admin.gpgtrustedkeys.GpgTrustedKeysPage;
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
import io.onedev.server.web.page.admin.securitysetting.SecuritySettingPage;
import io.onedev.server.web.page.admin.serverinformation.ServerInformationPage;
import io.onedev.server.web.page.admin.serverlog.ServerLogPage;
import io.onedev.server.web.page.admin.servicedesk.ServiceDeskSettingPage;
import io.onedev.server.web.page.admin.sshserverkey.SshServerKeyPage;
import io.onedev.server.web.page.admin.ssosetting.SsoConnectorListPage;
import io.onedev.server.web.page.admin.ssosetting.SsoProcessPage;
import io.onedev.server.web.page.admin.systemsetting.SystemSettingPage;
import io.onedev.server.web.page.admin.user.UserListPage;
import io.onedev.server.web.page.admin.user.accesstoken.UserAccessTokenPage;
import io.onedev.server.web.page.admin.user.authorization.UserAuthorizationsPage;
import io.onedev.server.web.page.admin.user.avatar.UserAvatarPage;
import io.onedev.server.web.page.admin.user.create.NewUserPage;
import io.onedev.server.web.page.admin.user.emailaddresses.UserEmailAddressesPage;
import io.onedev.server.web.page.admin.user.gpgkeys.UserGpgKeysPage;
import io.onedev.server.web.page.admin.user.membership.UserMembershipsPage;
import io.onedev.server.web.page.admin.user.password.UserPasswordPage;
import io.onedev.server.web.page.admin.user.profile.UserProfilePage;
import io.onedev.server.web.page.admin.user.sshkeys.UserSshKeysPage;
import io.onedev.server.web.page.admin.user.twofactorauthentication.UserTwoFactorAuthenticationPage;
import io.onedev.server.web.page.builds.BuildListPage;
import io.onedev.server.web.page.help.IncompatibilitiesPage;
import io.onedev.server.web.page.help.MethodDetailPage;
import io.onedev.server.web.page.help.ResourceDetailPage;
import io.onedev.server.web.page.help.ResourceListPage;
import io.onedev.server.web.page.issues.IssueListPage;
import io.onedev.server.web.page.my.accesstoken.MyAccessTokenPage;
import io.onedev.server.web.page.my.avatar.MyAvatarPage;
import io.onedev.server.web.page.my.emailaddresses.MyEmailAddressesPage;
import io.onedev.server.web.page.my.gpgkeys.MyGpgKeysPage;
import io.onedev.server.web.page.my.password.MyPasswordPage;
import io.onedev.server.web.page.my.profile.MyProfilePage;
import io.onedev.server.web.page.my.sshkeys.MySshKeysPage;
import io.onedev.server.web.page.my.twofactorauthentication.MyTwoFactorAuthenticationPage;
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
import io.onedev.server.web.page.project.issues.detail.IssueAuthorizationsPage;
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
import io.onedev.server.web.page.simple.security.EmailAddressVerificationPage;
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
import io.onedev.server.web.resource.SpriteResourceReference;

public class BaseUrlMapper extends CompoundRequestMapper {

	@Override
	public CompoundRequestMapper add(IRequestMapper mapper) {
		if (mapper instanceof ResourceMapper && !(mapper instanceof BaseResourceMapper))
			throw new ExplicitException("Base resource mapper should be used");
		return super.add(mapper);
	}

	public BaseUrlMapper(WebApplication app) {
		add(new BasePageMapper("init", ServerInitPage.class));
		add(new BasePageMapper("loading", BrowserInfoPage.class));
		addProjectPages();
		add(new BasePageMapper("issues", IssueListPage.class));
		add(new BasePageMapper("pull-requests", PullRequestListPage.class));
		add(new BasePageMapper("builds", BuildListPage.class));
		addAdministrationPages();
		addMyPages();
		addSecurityPages();
		addResources();
		addHelpPages();
		addErrorPages();
	}

	private void addHelpPages() {
		add(new BasePageMapper("help/incompatibilities", IncompatibilitiesPage.class));
		add(new BasePageMapper("help/api", ResourceListPage.class));
		add(new BasePageMapper("help/api/${resource}", ResourceDetailPage.class));
		add(new BasePageMapper("help/api/${resource}/${method}", MethodDetailPage.class));
	}
	
	private void addMyPages() {
		add(new BasePageMapper("my", MyProfilePage.class));
		add(new BasePageMapper("my/email-addresses", MyEmailAddressesPage.class));
		add(new BasePageMapper("my/avatar", MyAvatarPage.class));
		add(new BasePageMapper("my/password", MyPasswordPage.class));
		add(new BasePageMapper("my/ssh-keys", MySshKeysPage.class));
		add(new BasePageMapper("my/gpg-keys", MyGpgKeysPage.class));
		add(new BasePageMapper("my/access-token", MyAccessTokenPage.class));
		add(new BasePageMapper("my/two-factor-authentication", MyTwoFactorAuthenticationPage.class));
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
		add(new BaseResourceMapper("projects/${project}/raw", new RawBlobResourceReference()));
		
		// Change AttachmentResource.authorizeGroup accordingly if change attachment url here
		add(new BaseResourceMapper("projects/${project}/attachment/${group}/${attachment}", 
				new AttachmentResourceReference()));
		
		add(new BaseResourceMapper("downloads/projects/${project}/builds/${build}/artifacts", 
				new ArtifactResourceReference()));
		add(new BaseResourceMapper(SpriteResourceReference.DEFAULT_MOUNT_PATH, 
				new SpriteResourceReference(IconScope.class)));
	}
	
	private void addErrorPages() {
		add(new BasePageMapper("/errors/404", PageNotFoundErrorPage.class));
		add(new BasePageMapper("/errors/405", MethodNotAllowedErrorPage.class));
	}
	
	private void addSecurityPages() {
		add(new BasePageMapper("login", LoginPage.class));
		add(new BasePageMapper("logout", LogoutPage.class));
		add(new BasePageMapper("signup", SignUpPage.class));
		add(new BasePageMapper("reset-password", PasswordResetPage.class));
		add(new BasePageMapper("verify-email-address/${emailAddress}/${verificationCode}", 
				EmailAddressVerificationPage.class));
		add(new BasePageMapper(SsoProcessPage.MOUNT_PATH + "/${stage}/${connector}", SsoProcessPage.class));
	}
 	
	private void addAdministrationPages() {
		add(new BasePageMapper("administration/settings/system", SystemSettingPage.class));
		add(new BasePageMapper("administration/settings/security", SecuritySettingPage.class));
		add(new BasePageMapper("administration/users", UserListPage.class));
		add(new BasePageMapper("administration/users/new", NewUserPage.class));
		add(new BasePageMapper("administration/users/${user}", UserProfilePage.class));
		add(new BasePageMapper("administration/users/${user}/email-setting", UserEmailAddressesPage.class));
		add(new BasePageMapper("administration/users/${user}/groups", UserMembershipsPage.class));
		add(new BasePageMapper("administration/users/${user}/authorizations", UserAuthorizationsPage.class));
		add(new BasePageMapper("administration/users/${user}/avatar", UserAvatarPage.class));
		add(new BasePageMapper("administration/users/${user}/password", UserPasswordPage.class));
		add(new BasePageMapper("administration/users/${user}/ssh-keys", UserSshKeysPage.class));
		add(new BasePageMapper("administration/users/${user}/gpg-keys", UserGpgKeysPage.class));
		add(new BasePageMapper("administration/users/${user}/access-token", UserAccessTokenPage.class));
		add(new BasePageMapper("administration/users/${user}/two-factor-authentication", UserTwoFactorAuthenticationPage.class));
		
		add(new BasePageMapper("administration/roles", RoleListPage.class));
		add(new BasePageMapper("administration/roles/new", NewRolePage.class));
		add(new BasePageMapper("administration/roles/${role}", RoleDetailPage.class));
		
		add(new BasePageMapper("administration/groups", GroupListPage.class));
		add(new BasePageMapper("administration/groups/new", NewGroupPage.class));
		add(new BasePageMapper("administration/groups/${group}", GroupProfilePage.class));
		add(new BasePageMapper("administration/groups/${group}/members", GroupMembershipsPage.class));
		add(new BasePageMapper("administration/groups/${group}/authorizations", GroupAuthorizationsPage.class));
		
		add(new BasePageMapper("administration/settings/mail", MailSettingPage.class));
		add(new BasePageMapper("administration/settings/service-desk-setting", 
				ServiceDeskSettingPage.class));
		add(new BasePageMapper("administration/settings/issue-notification-template", 
				IssueNotificationTemplatePage.class));
		add(new BasePageMapper("administration/settings/pull-request-notification-template", 
				PullRequestNotificationTemplatePage.class));
		add(new BasePageMapper("administration/settings/performance", PerformanceSettingPage.class));
		add(new BasePageMapper("administration/settings/backup", DatabaseBackupPage.class));
		add(new BasePageMapper("administration/settings/authenticator", AuthenticatorPage.class));
		add(new BasePageMapper("administration/settings/sso-connectors", SsoConnectorListPage.class));
		add(new BasePageMapper("administration/settings/ssh-server-key", SshServerKeyPage.class));
 		add(new BasePageMapper("administration/settings/gpg-signing-key", GpgSigningKeyPage.class));
 		add(new BasePageMapper("administration/settings/gpg-trusted-keys", GpgTrustedKeysPage.class));

		add(new BasePageMapper("administration/agents", AgentListPage.class));
		add(new BasePageMapper("administration/agents/${agent}", AgentOverviewPage.class));
		add(new BasePageMapper("administration/agents/${agent}/builds", AgentBuildsPage.class));
		add(new BasePageMapper("administration/agents/${agent}/log", AgentLogPage.class));
		add(new BasePageMapper("administration/settings/job-executors", JobExecutorsPage.class));
		add(new BasePageMapper("administration/settings/groovy-scripts", GroovyScriptListPage.class));
		
		add(new BasePageMapper("administration/settings/issue-fields", IssueFieldListPage.class));
		add(new BasePageMapper("administration/settings/issue-states", IssueStateListPage.class));
		add(new BasePageMapper("administration/settings/state-transitions", StateTransitionListPage.class));
		add(new BasePageMapper("administration/settings/issue-boards", DefaultBoardListPage.class));
		add(new BasePageMapper("administration/settings/issue-links", LinkSpecListPage.class));
		add(new BasePageMapper("administration/settings/issue-templates", IssueTemplateListPage.class));
		
		add(new BasePageMapper("administration/settings/branding", BrandingSettingPage.class));
		
		add(new BasePageMapper("administration/settings/${setting}", ContributedAdministrationSettingPage.class));
		
		add(new BasePageMapper("administration/server-log", ServerLogPage.class));
		add(new BasePageMapper("administration/server-information", ServerInformationPage.class));
	}
	
	private void addProjectPages() {
		add(new BasePageMapper("projects", ProjectListPage.class));
		add(new BasePageMapper("projects/new", NewProjectPage.class));
		add(new BasePageMapper("projects/import/${importer}", ProjectImportPage.class));
		add(new BasePageMapper("projects/${project}", ProjectDashboardPage.class));

		// keep this url here for backward compatibility
		add(new BasePageMapper("projects/${project}/blob", ProjectBlobPage.class));
		
		add(new BasePageMapper("projects/${project}/files", ProjectBlobPage.class));
		add(new BasePageMapper("projects/${project}/commits", ProjectCommitsPage.class));
		add(new BasePageMapper("projects/${project}/commits/${commit}", CommitDetailPage.class));
		add(new BasePageMapper("projects/${project}/compare", RevisionComparePage.class));
		add(new BasePageMapper("projects/${project}/stats/contribs", ProjectContribsPage.class));
		add(new BasePageMapper("projects/${project}/stats/lines", SourceLinesPage.class));

		add(new BasePageMapper("projects/${project}/branches", ProjectBranchesPage.class));
		add(new BasePageMapper("projects/${project}/tags", ProjectTagsPage.class));
		add(new BasePageMapper("projects/${project}/code-comments", ProjectCodeCommentsPage.class));
		add(new BasePageMapper("projects/${project}/code-comments/${code-comment}/invalid", InvalidCodeCommentPage.class));

		add(new BasePageMapper("projects/${project}/pulls", ProjectPullRequestsPage.class));
		add(new BasePageMapper("projects/${project}/pulls/new", NewPullRequestPage.class));
		add(new BasePageMapper("projects/${project}/pulls/${request}", PullRequestActivitiesPage.class));
		add(new BasePageMapper("projects/${project}/pulls/${request}/code-comments", PullRequestCodeCommentsPage.class));
		add(new BasePageMapper("projects/${project}/pulls/${request}/changes", PullRequestChangesPage.class));
		add(new BasePageMapper("projects/${project}/pulls/${request}/invalid", InvalidPullRequestPage.class));

		add(new BasePageMapper("projects/${project}/boards", IssueBoardsPage.class));
		add(new BasePageMapper("projects/${project}/boards/${board}", IssueBoardsPage.class));
		add(new BasePageMapper("projects/${project}/issues", ProjectIssueListPage.class));
		add(new BasePageMapper("projects/${project}/issues/${issue}", IssueActivitiesPage.class));
		add(new BasePageMapper("projects/${project}/issues/${issue}/commits", IssueCommitsPage.class));
		add(new BasePageMapper("projects/${project}/issues/${issue}/pull-requests", IssuePullRequestsPage.class));
		add(new BasePageMapper("projects/${project}/issues/${issue}/builds", IssueBuildsPage.class));
		add(new BasePageMapper("projects/${project}/issues/${issue}/authorizations", IssueAuthorizationsPage.class));
		add(new BasePageMapper("projects/${project}/issues/new", NewIssuePage.class));
		add(new BasePageMapper("projects/${project}/issues/import/${importer}", IssueImportPage.class));
		add(new BasePageMapper("projects/${project}/milestones", MilestoneListPage.class));
		add(new BasePageMapper("projects/${project}/milestones/${milestone}", MilestoneIssuesPage.class));
		add(new BasePageMapper("projects/${project}/milestones/${milestone}/burndown", MilestoneBurndownPage.class));
		add(new BasePageMapper("projects/${project}/milestones/${milestone}/edit", MilestoneEditPage.class));
		add(new BasePageMapper("projects/${project}/milestones/new", NewMilestonePage.class));
		
		add(new BasePageMapper("projects/${project}/builds", ProjectBuildsPage.class));
		add(new BasePageMapper("projects/${project}/builds/${build}", BuildDashboardPage.class));
		add(new BasePageMapper("projects/${project}/builds/${build}/pipeline", BuildPipelinePage.class));
		add(new BasePageMapper("projects/${project}/builds/${build}/log", BuildLogPage.class));
		add(new BasePageMapper("projects/${project}/builds/${build}/changes", BuildChangesPage.class));
		add(new BasePageMapper("projects/${project}/builds/${build}/fixed-issues", FixedIssuesPage.class));
		add(new BasePageMapper("projects/${project}/builds/${build}/artifacts", BuildArtifactsPage.class));
		add(new BasePageMapper("projects/${project}/builds/${build}/invalid", InvalidBuildPage.class));
		
		add(new BasePageMapper("projects/${project}/children", ProjectChildrenPage.class));
		
		add(new BasePageMapper("projects/${project}/settings/general", GeneralProjectSettingPage.class));
		add(new BasePageMapper("projects/${project}/settings/authorizations", ProjectAuthorizationsPage.class));
		add(new BasePageMapper("projects/${project}/settings/avatar-edit", AvatarEditPage.class));
		add(new BasePageMapper("projects/${project}/settings/branch-protection", BranchProtectionsPage.class));
		add(new BasePageMapper("projects/${project}/settings/tag-protection", TagProtectionsPage.class));
		add(new BasePageMapper("projects/${project}/settings/build/job-secrets", JobSecretsPage.class));
		add(new BasePageMapper("projects/${project}/settings/build/action-authorizations", ActionAuthorizationsPage.class));
		add(new BasePageMapper("projects/${project}/settings/build/build-preserve-rules", BuildPreservationsPage.class));
		add(new BasePageMapper("projects/${project}/settings/build/default-fixed-issues-filter", DefaultFixedIssueFiltersPage.class));
		add(new BasePageMapper("projects/${project}/settings/service-desk", ProjectServiceDeskSettingPage.class));
		add(new BasePageMapper("projects/${project}/settings/web-hooks", WebHooksPage.class));
		add(new BasePageMapper("projects/${project}/settings/${setting}", ContributedProjectSettingPage.class));
	}

}
