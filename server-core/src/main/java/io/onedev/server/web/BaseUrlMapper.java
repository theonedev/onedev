package io.onedev.server.web;

import org.apache.wicket.core.request.mapper.ResourceMapper;
import org.apache.wicket.markup.html.pages.BrowserInfoPage;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.mapper.CompoundRequestMapper;

import io.onedev.server.web.download.ArchiveDownloadResourceReference;
import io.onedev.server.web.download.ArtifactDownloadResourceReference;
import io.onedev.server.web.download.AttachmentDownloadResourceReference;
import io.onedev.server.web.download.BuildLogDownloadResourceReference;
import io.onedev.server.web.download.RawBlobDownloadResourceReference;
import io.onedev.server.web.download.ServerLogDownloadResourceReference;
import io.onedev.server.web.mapper.GeneralPageMapper;
import io.onedev.server.web.mapper.GeneralResourceMapper;
import io.onedev.server.web.page.admin.authenticator.AuthenticatorPage;
import io.onedev.server.web.page.admin.databasebackup.DatabaseBackupPage;
import io.onedev.server.web.page.admin.groovyscript.GroovyScriptListPage;
import io.onedev.server.web.page.admin.group.GroupListPage;
import io.onedev.server.web.page.admin.group.authorization.GroupAuthorizationsPage;
import io.onedev.server.web.page.admin.group.create.NewGroupPage;
import io.onedev.server.web.page.admin.group.membership.GroupMembershipsPage;
import io.onedev.server.web.page.admin.group.profile.GroupProfilePage;
import io.onedev.server.web.page.admin.issuesetting.defaultboard.DefaultBoardListPage;
import io.onedev.server.web.page.admin.issuesetting.fieldspec.IssueFieldListPage;
import io.onedev.server.web.page.admin.issuesetting.issuetemplate.IssueTemplateListPage;
import io.onedev.server.web.page.admin.issuesetting.statespec.IssueStateListPage;
import io.onedev.server.web.page.admin.issuesetting.transitionspec.StateTransitionsPage;
import io.onedev.server.web.page.admin.jobexecutor.JobExecutorsPage;
import io.onedev.server.web.page.admin.mailsetting.MailSettingPage;
import io.onedev.server.web.page.admin.role.NewRolePage;
import io.onedev.server.web.page.admin.role.RoleDetailPage;
import io.onedev.server.web.page.admin.role.RoleListPage;
import io.onedev.server.web.page.admin.securitysetting.SecuritySettingPage;
import io.onedev.server.web.page.admin.serverinformation.ServerInformationPage;
import io.onedev.server.web.page.admin.serverlog.ServerLogPage;
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
import io.onedev.server.web.page.init.ServerInitPage;
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
import io.onedev.server.web.page.project.codecomments.InvalidCodeCommentPage;
import io.onedev.server.web.page.project.codecomments.ProjectCodeCommentsPage;
import io.onedev.server.web.page.project.commits.CommitDetailPage;
import io.onedev.server.web.page.project.commits.ProjectCommitsPage;
import io.onedev.server.web.page.project.compare.RevisionComparePage;
import io.onedev.server.web.page.project.dashboard.ProjectDashboardPage;
import io.onedev.server.web.page.project.issues.boards.IssueBoardsPage;
import io.onedev.server.web.page.project.issues.create.NewIssuePage;
import io.onedev.server.web.page.project.issues.detail.IssueActivitiesPage;
import io.onedev.server.web.page.project.issues.detail.IssueBuildsPage;
import io.onedev.server.web.page.project.issues.detail.IssueCommitsPage;
import io.onedev.server.web.page.project.issues.detail.IssuePullRequestsPage;
import io.onedev.server.web.page.project.issues.list.ProjectIssueListPage;
import io.onedev.server.web.page.project.issues.milestones.MilestoneDetailPage;
import io.onedev.server.web.page.project.issues.milestones.MilestoneEditPage;
import io.onedev.server.web.page.project.issues.milestones.MilestoneListPage;
import io.onedev.server.web.page.project.issues.milestones.NewMilestonePage;
import io.onedev.server.web.page.project.pullrequests.InvalidPullRequestPage;
import io.onedev.server.web.page.project.pullrequests.ProjectPullRequestsPage;
import io.onedev.server.web.page.project.pullrequests.create.NewPullRequestPage;
import io.onedev.server.web.page.project.pullrequests.detail.activities.PullRequestActivitiesPage;
import io.onedev.server.web.page.project.pullrequests.detail.changes.PullRequestChangesPage;
import io.onedev.server.web.page.project.pullrequests.detail.codecomments.PullRequestCodeCommentsPage;
import io.onedev.server.web.page.project.pullrequests.detail.mergepreview.MergePreviewPage;
import io.onedev.server.web.page.project.setting.authorization.ProjectAuthorizationsPage;
import io.onedev.server.web.page.project.setting.avatar.AvatarEditPage;
import io.onedev.server.web.page.project.setting.branchprotection.BranchProtectionsPage;
import io.onedev.server.web.page.project.setting.build.ActionAuthorizationsPage;
import io.onedev.server.web.page.project.setting.build.BuildPreservationsPage;
import io.onedev.server.web.page.project.setting.build.JobSecretsPage;
import io.onedev.server.web.page.project.setting.general.GeneralSettingPage;
import io.onedev.server.web.page.project.setting.tagprotection.TagProtectionsPage;
import io.onedev.server.web.page.project.setting.webhook.WebHooksPage;
import io.onedev.server.web.page.project.stats.ProjectContribsPage;
import io.onedev.server.web.page.project.stats.SourceLinesPage;
import io.onedev.server.web.page.project.tags.ProjectTagsPage;
import io.onedev.server.web.page.pullrequests.PullRequestListPage;
import io.onedev.server.web.page.security.ForgetPage;
import io.onedev.server.web.page.security.LoginPage;
import io.onedev.server.web.page.security.LogoutPage;
import io.onedev.server.web.page.security.RegisterPage;

public class BaseUrlMapper extends CompoundRequestMapper {

	public BaseUrlMapper(WebApplication app) {
		add(new GeneralPageMapper("init", ServerInitPage.class));
		add(new GeneralPageMapper("loading", BrowserInfoPage.class));
		add(new GeneralPageMapper("issues", IssueListPage.class));
		add(new GeneralPageMapper("pull-requests", PullRequestListPage.class));
		add(new GeneralPageMapper("builds", BuildListPage.class));
		addProjectPages();
		addMyPages();
		addAdministrationPages();
		addSecurityPages();
				
		addResources();
	}

	private void addMyPages() {
		add(new GeneralPageMapper("my/profile", MyProfilePage.class));
		add(new GeneralPageMapper("my/avatar", MyAvatarPage.class));
		add(new GeneralPageMapper("my/password", MyPasswordPage.class));
		add(new GeneralPageMapper("my/ssh-keys", MySshKeysPage.class));
		add(new GeneralPageMapper("my/access-token", MyAccessTokenPage.class));
	}

	private void addResources() {
		add(new ResourceMapper("downloads/server-log", new ServerLogDownloadResourceReference()));
		add(new ResourceMapper("downloads/projects/${project}/builds/${build}/log", new BuildLogDownloadResourceReference()));
		add(new ResourceMapper("projects/${project}/archive/${revision}", new ArchiveDownloadResourceReference()));
		add(new GeneralResourceMapper("projects/${project}/raw/${revision}/${path}", new RawBlobDownloadResourceReference()));
		add(new ResourceMapper("projects/${project}/attachment/${uuid}/${attachment}", new AttachmentDownloadResourceReference()));
		add(new GeneralResourceMapper("downloads/projects/${project}/builds/${build}/artifacts/${path}", 
				new ArtifactDownloadResourceReference()));
	}
	
	private void addSecurityPages() {
		add(new GeneralPageMapper("login", LoginPage.class));
		add(new GeneralPageMapper("logout", LogoutPage.class));
		add(new GeneralPageMapper("register", RegisterPage.class));
		add(new GeneralPageMapper("forget", ForgetPage.class));
		add(new GeneralPageMapper(SsoProcessPage.MOUNTED_PATH + "/${stage}/${connector}", SsoProcessPage.class));
	}
 	
	private void addAdministrationPages() {
		add(new GeneralPageMapper("administration", UserListPage.class));
		add(new GeneralPageMapper("administration/users", UserListPage.class));
		add(new GeneralPageMapper("administration/users/new", NewUserPage.class));
		add(new GeneralPageMapper("administration/users/${user}", UserProfilePage.class));
		add(new GeneralPageMapper("administration/users/${user}/profile", UserProfilePage.class));
		add(new GeneralPageMapper("administration/users/${user}/groups", UserMembershipsPage.class));
		add(new GeneralPageMapper("administration/users/${user}/authorizations", UserAuthorizationsPage.class));
		add(new GeneralPageMapper("administration/users/${user}/avatar", UserAvatarPage.class));
		add(new GeneralPageMapper("administration/users/${user}/password", UserPasswordPage.class));
		add(new GeneralPageMapper("administration/users/${user}/ssh-keys", UserSshKeysPage.class));
		add(new GeneralPageMapper("administration/users/${user}/access-token", UserAccessTokenPage.class));
		
		add(new GeneralPageMapper("administration/roles", RoleListPage.class));
		add(new GeneralPageMapper("administration/roles/new", NewRolePage.class));
		add(new GeneralPageMapper("administration/roles/${role}", RoleDetailPage.class));
		
		add(new GeneralPageMapper("administration/groups", GroupListPage.class));
		add(new GeneralPageMapper("administration/groups/new", NewGroupPage.class));
		add(new GeneralPageMapper("administration/groups/${group}", GroupProfilePage.class));
		add(new GeneralPageMapper("administration/groups/${group}/profile", GroupProfilePage.class));
		add(new GeneralPageMapper("administration/groups/${group}/members", GroupMembershipsPage.class));
		add(new GeneralPageMapper("administration/groups/${group}/authorizations", GroupAuthorizationsPage.class));
		
		add(new GeneralPageMapper("administration/settings", SystemSettingPage.class));
		add(new GeneralPageMapper("administration/settings/system", SystemSettingPage.class));
		add(new GeneralPageMapper("administration/settings/mail", MailSettingPage.class));
		add(new GeneralPageMapper("administration/settings/backup", DatabaseBackupPage.class));
		add(new GeneralPageMapper("administration/settings/security", SecuritySettingPage.class));
		add(new GeneralPageMapper("administration/settings/authenticator", AuthenticatorPage.class));
		add(new GeneralPageMapper("administration/settings/sso-connectors", SsoConnectorListPage.class));
		add(new GeneralPageMapper("administration/settings/ssh", SshSettingPage.class));

		add(new GeneralPageMapper("administration/settings/job-executors", JobExecutorsPage.class));
		add(new GeneralPageMapper("administration/settings/groovy-scripts", GroovyScriptListPage.class));
		
		add(new GeneralPageMapper("administration/settings/issue-fields", IssueFieldListPage.class));
		add(new GeneralPageMapper("administration/settings/issue-states", IssueStateListPage.class));
		add(new GeneralPageMapper("administration/settings/state-transitions", StateTransitionsPage.class));
		add(new GeneralPageMapper("administration/settings/issue-boards", DefaultBoardListPage.class));
		add(new GeneralPageMapper("administration/settings/issue-templates", IssueTemplateListPage.class));
		
		add(new GeneralPageMapper("administration/server-log", ServerLogPage.class));
		add(new GeneralPageMapper("administration/server-information", ServerInformationPage.class));
	}
	
	private void addProjectPages() {
		add(new GeneralPageMapper("projects", ProjectListPage.class));
		add(new GeneralPageMapper("projects/new", NewProjectPage.class));
		add(new GeneralPageMapper("projects/${project}", ProjectDashboardPage.class));

		add(new GeneralPageMapper("projects/${project}/blob/#{revision}/#{path}", ProjectBlobPage.class));
		add(new GeneralPageMapper("projects/${project}/commits", ProjectCommitsPage.class));
		add(new GeneralPageMapper("projects/${project}/commits/${revision}", CommitDetailPage.class));
		add(new GeneralPageMapper("projects/${project}/compare", RevisionComparePage.class));
		add(new GeneralPageMapper("projects/${project}/stats", ProjectContribsPage.class));
		add(new GeneralPageMapper("projects/${project}/stats/contribs", ProjectContribsPage.class));
		add(new GeneralPageMapper("projects/${project}/stats/lines", SourceLinesPage.class));

		add(new GeneralPageMapper("projects/${project}/branches", ProjectBranchesPage.class));
		add(new GeneralPageMapper("projects/${project}/tags", ProjectTagsPage.class));
		add(new GeneralPageMapper("projects/${project}/code-comments", ProjectCodeCommentsPage.class));
		add(new GeneralPageMapper("projects/${project}/code-comments/${code-comment}/invalid", InvalidCodeCommentPage.class));

		add(new GeneralPageMapper("projects/${project}/pulls", ProjectPullRequestsPage.class));
		add(new GeneralPageMapper("projects/${project}/pulls/new", NewPullRequestPage.class));
		add(new GeneralPageMapper("projects/${project}/pulls/${request}", PullRequestActivitiesPage.class));
		add(new GeneralPageMapper("projects/${project}/pulls/${request}/activities", PullRequestActivitiesPage.class));
		add(new GeneralPageMapper("projects/${project}/pulls/${request}/code-comments", PullRequestCodeCommentsPage.class));
		add(new GeneralPageMapper("projects/${project}/pulls/${request}/changes", PullRequestChangesPage.class));
		add(new GeneralPageMapper("projects/${project}/pulls/${request}/merge-preview", MergePreviewPage.class));
		add(new GeneralPageMapper("projects/${project}/pulls/${request}/invalid", InvalidPullRequestPage.class));

		add(new GeneralPageMapper("projects/${project}/boards", IssueBoardsPage.class));
		add(new GeneralPageMapper("projects/${project}/boards/${board}", IssueBoardsPage.class));
		add(new GeneralPageMapper("projects/${project}/issues", ProjectIssueListPage.class));
		add(new GeneralPageMapper("projects/${project}/issues/${issue}", IssueActivitiesPage.class));
		add(new GeneralPageMapper("projects/${project}/issues/${issue}/activities", IssueActivitiesPage.class));
		add(new GeneralPageMapper("projects/${project}/issues/${issue}/commits", IssueCommitsPage.class));
		add(new GeneralPageMapper("projects/${project}/issues/${issue}/pull-requests", IssuePullRequestsPage.class));
		add(new GeneralPageMapper("projects/${project}/issues/${issue}/builds", IssueBuildsPage.class));
		add(new GeneralPageMapper("projects/${project}/issues/new", NewIssuePage.class));
		add(new GeneralPageMapper("projects/${project}/milestones", MilestoneListPage.class));
		add(new GeneralPageMapper("projects/${project}/milestones/${milestone}", MilestoneDetailPage.class));
		add(new GeneralPageMapper("projects/${project}/milestones/${milestone}/edit", MilestoneEditPage.class));
		add(new GeneralPageMapper("projects/${project}/milestones/new", NewMilestonePage.class));
		
		add(new GeneralPageMapper("projects/${project}/builds", ProjectBuildsPage.class));
		add(new GeneralPageMapper("projects/${project}/builds/${build}", BuildDashboardPage.class));
		add(new GeneralPageMapper("projects/${project}/builds/${build}/log", BuildLogPage.class));
		add(new GeneralPageMapper("projects/${project}/builds/${build}/changes", BuildChangesPage.class));
		add(new GeneralPageMapper("projects/${project}/builds/${build}/fixed-issues", FixedIssuesPage.class));
		add(new GeneralPageMapper("projects/${project}/builds/${build}/artifacts", BuildArtifactsPage.class));
		add(new GeneralPageMapper("projects/${project}/builds/${build}/invalid", InvalidBuildPage.class));
		
		add(new GeneralPageMapper("projects/${project}/settings", GeneralSettingPage.class));
		add(new GeneralPageMapper("projects/${project}/settings/general", GeneralSettingPage.class));
		add(new GeneralPageMapper("projects/${project}/settings/authorizations", ProjectAuthorizationsPage.class));
		add(new GeneralPageMapper("projects/${project}/settings/avatar-edit", AvatarEditPage.class));
		add(new GeneralPageMapper("projects/${project}/settings/branch-protection", BranchProtectionsPage.class));
		add(new GeneralPageMapper("projects/${project}/settings/tag-protection", TagProtectionsPage.class));
		add(new GeneralPageMapper("projects/${project}/settings/build/job-secrets", JobSecretsPage.class));
		add(new GeneralPageMapper("projects/${project}/settings/build/action-authorizations", ActionAuthorizationsPage.class));
		add(new GeneralPageMapper("projects/${project}/settings/build/build-preserve-rules", BuildPreservationsPage.class));
		add(new GeneralPageMapper("projects/${project}/settings/web-hooks", WebHooksPage.class));
	}

}
