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
import io.onedev.server.web.mapper.OnePageMapper;
import io.onedev.server.web.mapper.OneResourceMapper;
import io.onedev.server.web.page.admin.authenticator.AuthenticatorPage;
import io.onedev.server.web.page.admin.databasebackup.DatabaseBackupPage;
import io.onedev.server.web.page.admin.groovyscript.GroovyScriptListPage;
import io.onedev.server.web.page.admin.group.GroupListPage;
import io.onedev.server.web.page.admin.group.authorization.GroupAuthorizationsPage;
import io.onedev.server.web.page.admin.group.create.NewGroupPage;
import io.onedev.server.web.page.admin.group.membership.GroupMembershipsPage;
import io.onedev.server.web.page.admin.group.profile.GroupProfilePage;
import io.onedev.server.web.page.admin.issuesetting.DefaultBoardListPage;
import io.onedev.server.web.page.admin.issuesetting.DefaultStateTransitionsPage;
import io.onedev.server.web.page.admin.issuesetting.IssueFieldListPage;
import io.onedev.server.web.page.admin.issuesetting.IssueStateListPage;
import io.onedev.server.web.page.admin.jobexecutor.JobExecutorsPage;
import io.onedev.server.web.page.admin.mailsetting.MailSettingPage;
import io.onedev.server.web.page.admin.role.NewRolePage;
import io.onedev.server.web.page.admin.role.RoleDetailPage;
import io.onedev.server.web.page.admin.role.RoleListPage;
import io.onedev.server.web.page.admin.securitysetting.SecuritySettingPage;
import io.onedev.server.web.page.admin.serverinformation.ServerInformationPage;
import io.onedev.server.web.page.admin.serverlog.ServerLogPage;
import io.onedev.server.web.page.admin.systemsetting.SystemSettingPage;
import io.onedev.server.web.page.admin.user.UserListPage;
import io.onedev.server.web.page.admin.user.authorization.UserAuthorizationsPage;
import io.onedev.server.web.page.admin.user.avatar.UserAvatarPage;
import io.onedev.server.web.page.admin.user.buildsetting.UserActionAuthorizationsPage;
import io.onedev.server.web.page.admin.user.buildsetting.UserBuildPreservationsPage;
import io.onedev.server.web.page.admin.user.buildsetting.UserJobSecretsPage;
import io.onedev.server.web.page.admin.user.create.NewUserPage;
import io.onedev.server.web.page.admin.user.membership.UserMembershipsPage;
import io.onedev.server.web.page.admin.user.password.UserPasswordPage;
import io.onedev.server.web.page.admin.user.profile.UserProfilePage;
import io.onedev.server.web.page.admin.user.webhook.UserWebHooksPage;
import io.onedev.server.web.page.build.BuildListPage;
import io.onedev.server.web.page.init.ServerInitPage;
import io.onedev.server.web.page.issue.IssueListPage;
import io.onedev.server.web.page.my.avatar.MyAvatarPage;
import io.onedev.server.web.page.my.buildsetting.MyActionAuthorizationsPage;
import io.onedev.server.web.page.my.buildsetting.MyBuildPreservationsPage;
import io.onedev.server.web.page.my.buildsetting.MyJobSecretsPage;
import io.onedev.server.web.page.my.password.MyPasswordPage;
import io.onedev.server.web.page.my.profile.MyProfilePage;
import io.onedev.server.web.page.my.webhook.MyWebHooksPage;
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
import io.onedev.server.web.page.project.codecomments.ProjectCodeCommentsPage;
import io.onedev.server.web.page.project.commits.CommitDetailPage;
import io.onedev.server.web.page.project.commits.ProjectCommitsPage;
import io.onedev.server.web.page.project.compare.RevisionComparePage;
import io.onedev.server.web.page.project.dashboard.ProjectDashboardPage;
import io.onedev.server.web.page.project.issues.boards.IssueBoardsPage;
import io.onedev.server.web.page.project.issues.create.NewIssuePage;
import io.onedev.server.web.page.project.issues.detail.IssueBuildsPage;
import io.onedev.server.web.page.project.issues.detail.IssueActivitiesPage;
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
import io.onedev.server.web.page.project.setting.build.ProjectActionAuthorizationsPage;
import io.onedev.server.web.page.project.setting.build.ProjectBuildPreservationsPage;
import io.onedev.server.web.page.project.setting.build.ProjectJobSecretsPage;
import io.onedev.server.web.page.project.setting.general.GeneralSettingPage;
import io.onedev.server.web.page.project.setting.issue.PromptFieldsUponIssueOpenSettingPage;
import io.onedev.server.web.page.project.setting.issue.StateTransitionsPage;
import io.onedev.server.web.page.project.setting.tagprotection.TagProtectionsPage;
import io.onedev.server.web.page.project.setting.webhook.ProjectWebHooksPage;
import io.onedev.server.web.page.project.stats.ProjectContribsPage;
import io.onedev.server.web.page.project.stats.SourceLinesPage;
import io.onedev.server.web.page.project.tags.ProjectTagsPage;
import io.onedev.server.web.page.pullrequest.PullRequestListPage;
import io.onedev.server.web.page.security.ForgetPage;
import io.onedev.server.web.page.security.LoginPage;
import io.onedev.server.web.page.security.LogoutPage;
import io.onedev.server.web.page.security.RegisterPage;

public class OneUrlMapper extends CompoundRequestMapper {

	public OneUrlMapper(WebApplication app) {
		add(new OnePageMapper("init", ServerInitPage.class));
		add(new OnePageMapper("loading", BrowserInfoPage.class));
		add(new OnePageMapper("issues", IssueListPage.class));
		add(new OnePageMapper("pull-requests", PullRequestListPage.class));
		add(new OnePageMapper("builds", BuildListPage.class));
		addProjectPages();
		addMyPages();
		addAdministrationPages();
		addSecurityPages();
				
		addResources();
	}

	private void addMyPages() {
		add(new OnePageMapper("my/profile", MyProfilePage.class));
		add(new OnePageMapper("my/avatar", MyAvatarPage.class));
		add(new OnePageMapper("my/password", MyPasswordPage.class));
		add(new OnePageMapper("my/build-setting/job-secrets", MyJobSecretsPage.class));
		add(new OnePageMapper("my/build-setting/action-authorizations", MyActionAuthorizationsPage.class));
		add(new OnePageMapper("my/build-setting/build-preserve-rules", MyBuildPreservationsPage.class));
		add(new OnePageMapper("my/web-hooks", MyWebHooksPage.class));
	}

	private void addResources() {
		add(new ResourceMapper("downloads/server-log", new ServerLogDownloadResourceReference()));
		add(new ResourceMapper("downloads/projects/${project}/builds/${build}/log", new BuildLogDownloadResourceReference()));
		add(new ResourceMapper("projects/${project}/archive/${revision}", new ArchiveDownloadResourceReference()));
		add(new OneResourceMapper("projects/${project}/raw/${revision}/${path}", new RawBlobDownloadResourceReference()));
		add(new ResourceMapper("projects/${project}/attachment/${uuid}/${attachment}", new AttachmentDownloadResourceReference()));
		add(new OneResourceMapper("downloads/projects/${project}/builds/${build}/artifacts/${path}", 
				new ArtifactDownloadResourceReference()));
	}
	
	private void addSecurityPages() {
		add(new OnePageMapper("login", LoginPage.class));
		add(new OnePageMapper("logout", LogoutPage.class));
		add(new OnePageMapper("register", RegisterPage.class));
		add(new OnePageMapper("forget", ForgetPage.class));
	}
	
	private void addAdministrationPages() {
		add(new OnePageMapper("administration", UserListPage.class));
		add(new OnePageMapper("administration/users", UserListPage.class));
		add(new OnePageMapper("administration/users/new", NewUserPage.class));
		add(new OnePageMapper("administration/users/${user}", UserProfilePage.class));
		add(new OnePageMapper("administration/users/${user}/profile", UserProfilePage.class));
		add(new OnePageMapper("administration/users/${user}/groups", UserMembershipsPage.class));
		add(new OnePageMapper("administration/users/${user}/authorizations", UserAuthorizationsPage.class));
		add(new OnePageMapper("administration/users/${user}/avatar", UserAvatarPage.class));
		add(new OnePageMapper("administration/users/${user}/password", UserPasswordPage.class));
		add(new OnePageMapper("administration/users/${user}/build-setting", UserJobSecretsPage.class));
		add(new OnePageMapper("administration/users/${user}/build-setting/job-secrets", UserJobSecretsPage.class));
		add(new OnePageMapper("administration/users/${user}/build-setting/action-authorizations", UserActionAuthorizationsPage.class));
		add(new OnePageMapper("administration/users/${user}/build-setting/build-preserve-rules", UserBuildPreservationsPage.class));
		add(new OnePageMapper("administration/users/${user}/web-hooks", UserWebHooksPage.class));
		
		add(new OnePageMapper("administration/roles", RoleListPage.class));
		add(new OnePageMapper("administration/roles/new", NewRolePage.class));
		add(new OnePageMapper("administration/roles/${role}", RoleDetailPage.class));
		
		add(new OnePageMapper("administration/groups", GroupListPage.class));
		add(new OnePageMapper("administration/groups/new", NewGroupPage.class));
		add(new OnePageMapper("administration/groups/${group}", GroupProfilePage.class));
		add(new OnePageMapper("administration/groups/${group}/profile", GroupProfilePage.class));
		add(new OnePageMapper("administration/groups/${group}/members", GroupMembershipsPage.class));
		add(new OnePageMapper("administration/groups/${group}/authorizations", GroupAuthorizationsPage.class));
		
		add(new OnePageMapper("administration/settings", SystemSettingPage.class));
		add(new OnePageMapper("administration/settings/system", SystemSettingPage.class));
		add(new OnePageMapper("administration/settings/mail", MailSettingPage.class));
		add(new OnePageMapper("administration/settings/backup", DatabaseBackupPage.class));
		add(new OnePageMapper("administration/settings/security", SecuritySettingPage.class));
		add(new OnePageMapper("administration/settings/authenticator", AuthenticatorPage.class));

		add(new OnePageMapper("administration/settings/job-executors", JobExecutorsPage.class));
		add(new OnePageMapper("administration/settings/groovy-scripts", GroovyScriptListPage.class));
		
		add(new OnePageMapper("administration/settings/issue-fields", IssueFieldListPage.class));
		add(new OnePageMapper("administration/settings/issue-states", IssueStateListPage.class));
		add(new OnePageMapper("administration/settings/state-transitions", DefaultStateTransitionsPage.class));
		add(new OnePageMapper("administration/settings/issue-boards", DefaultBoardListPage.class));
		
		add(new OnePageMapper("administration/server-log", ServerLogPage.class));
		add(new OnePageMapper("administration/server-information", ServerInformationPage.class));
	}
	
	private void addProjectPages() {
		add(new OnePageMapper("projects", ProjectListPage.class));
		add(new OnePageMapper("projects/new", NewProjectPage.class));
		add(new OnePageMapper("projects/${project}", ProjectDashboardPage.class));

		add(new OnePageMapper("projects/${project}/blob/#{revision}/#{path}", ProjectBlobPage.class));
		add(new OnePageMapper("projects/${project}/commits", ProjectCommitsPage.class));
		add(new OnePageMapper("projects/${project}/commits/${revision}", CommitDetailPage.class));
		add(new OnePageMapper("projects/${project}/compare", RevisionComparePage.class));
		add(new OnePageMapper("projects/${project}/stats", ProjectContribsPage.class));
		add(new OnePageMapper("projects/${project}/stats/contribs", ProjectContribsPage.class));
		add(new OnePageMapper("projects/${project}/stats/lines", SourceLinesPage.class));

		add(new OnePageMapper("projects/${project}/branches", ProjectBranchesPage.class));
		add(new OnePageMapper("projects/${project}/tags", ProjectTagsPage.class));
		add(new OnePageMapper("projects/${project}/codecomments", ProjectCodeCommentsPage.class));

		add(new OnePageMapper("projects/${project}/pulls", ProjectPullRequestsPage.class));
		add(new OnePageMapper("projects/${project}/pulls/new", NewPullRequestPage.class));
		add(new OnePageMapper("projects/${project}/pulls/${request}", PullRequestActivitiesPage.class));
		add(new OnePageMapper("projects/${project}/pulls/${request}/activities", PullRequestActivitiesPage.class));
		add(new OnePageMapper("projects/${project}/pulls/${request}/code-comments", PullRequestCodeCommentsPage.class));
		add(new OnePageMapper("projects/${project}/pulls/${request}/changes", PullRequestChangesPage.class));
		add(new OnePageMapper("projects/${project}/pulls/${request}/merge-preview", MergePreviewPage.class));
		add(new OnePageMapper("projects/${project}/pulls/${request}/invalid", InvalidPullRequestPage.class));

		add(new OnePageMapper("projects/${project}/boards", IssueBoardsPage.class));
		add(new OnePageMapper("projects/${project}/boards/${board}", IssueBoardsPage.class));
		add(new OnePageMapper("projects/${project}/issues", ProjectIssueListPage.class));
		add(new OnePageMapper("projects/${project}/issues/${issue}", IssueActivitiesPage.class));
		add(new OnePageMapper("projects/${project}/issues/${issue}/activities", IssueActivitiesPage.class));
		add(new OnePageMapper("projects/${project}/issues/${issue}/builds", IssueBuildsPage.class));
		add(new OnePageMapper("projects/${project}/issues/new", NewIssuePage.class));
		add(new OnePageMapper("projects/${project}/milestones", MilestoneListPage.class));
		add(new OnePageMapper("projects/${project}/milestones/${milestone}", MilestoneDetailPage.class));
		add(new OnePageMapper("projects/${project}/milestones/${milestone}/edit", MilestoneEditPage.class));
		add(new OnePageMapper("projects/${project}/milestones/new", NewMilestonePage.class));
		
		add(new OnePageMapper("projects/${project}/builds", ProjectBuildsPage.class));
		add(new OnePageMapper("projects/${project}/builds/${build}", BuildDashboardPage.class));
		add(new OnePageMapper("projects/${project}/builds/${build}/log", BuildLogPage.class));
		add(new OnePageMapper("projects/${project}/builds/${build}/changes", BuildChangesPage.class));
		add(new OnePageMapper("projects/${project}/builds/${build}/fixed-issues", FixedIssuesPage.class));
		add(new OnePageMapper("projects/${project}/builds/${build}/artifacts", BuildArtifactsPage.class));
		add(new OnePageMapper("projects/${project}/builds/${build}/invalid", InvalidBuildPage.class));
		
		add(new OnePageMapper("projects/${project}/settings", GeneralSettingPage.class));
		add(new OnePageMapper("projects/${project}/settings/general", GeneralSettingPage.class));
		add(new OnePageMapper("projects/${project}/settings/authorizations", ProjectAuthorizationsPage.class));
		add(new OnePageMapper("projects/${project}/settings/avatar-edit", AvatarEditPage.class));
		add(new OnePageMapper("projects/${project}/settings/branch-protection", BranchProtectionsPage.class));
		add(new OnePageMapper("projects/${project}/settings/tag-protection", TagProtectionsPage.class));
		add(new OnePageMapper("projects/${project}/settings/build/job-secrets", ProjectJobSecretsPage.class));
		add(new OnePageMapper("projects/${project}/settings/build/action-authorizations", ProjectActionAuthorizationsPage.class));
		add(new OnePageMapper("projects/${project}/settings/build/build-preserve-rules", ProjectBuildPreservationsPage.class));
		add(new OnePageMapper("projects/${project}/settings/issue/state-transitions", StateTransitionsPage.class));
		add(new OnePageMapper("projects/${project}/settings/issue/prompt-fields-upon-issue-open", 
				PromptFieldsUponIssueOpenSettingPage.class));
		add(new OnePageMapper("projects/${project}/settings/web-hooks", ProjectWebHooksPage.class));
	}

}
