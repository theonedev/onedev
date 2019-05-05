package io.onedev.server.web;

import org.apache.wicket.core.request.handler.BookmarkablePageRequestHandler;
import org.apache.wicket.core.request.handler.IPageClassRequestHandler;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler;
import org.apache.wicket.core.request.mapper.ResourceMapper;
import org.apache.wicket.markup.html.pages.BrowserInfoPage;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.mapper.CompoundRequestMapper;

import io.onedev.server.web.page.admin.authenticator.AuthenticatorPage;
import io.onedev.server.web.page.admin.databasebackup.DatabaseBackupPage;
import io.onedev.server.web.page.admin.group.GroupAuthorizationsPage;
import io.onedev.server.web.page.admin.group.GroupListPage;
import io.onedev.server.web.page.admin.group.GroupMembershipsPage;
import io.onedev.server.web.page.admin.group.GroupProfilePage;
import io.onedev.server.web.page.admin.group.NewGroupPage;
import io.onedev.server.web.page.admin.issuesetting.DefaultBoardListPage;
import io.onedev.server.web.page.admin.issuesetting.DefaultQueryListPage;
import io.onedev.server.web.page.admin.issuesetting.DefaultStateTransitionsPage;
import io.onedev.server.web.page.admin.issuesetting.IssueFieldListPage;
import io.onedev.server.web.page.admin.issuesetting.IssueStateListPage;
import io.onedev.server.web.page.admin.jobexecutor.JobExecutorPage;
import io.onedev.server.web.page.admin.mailsetting.MailSettingPage;
import io.onedev.server.web.page.admin.securitysetting.SecuritySettingPage;
import io.onedev.server.web.page.admin.serverinformation.ServerInformationPage;
import io.onedev.server.web.page.admin.serverlog.ServerLogPage;
import io.onedev.server.web.page.admin.systemsetting.SystemSettingPage;
import io.onedev.server.web.page.admin.user.NewUserPage;
import io.onedev.server.web.page.admin.user.UserAuthorizationsPage;
import io.onedev.server.web.page.admin.user.UserAvatarPage;
import io.onedev.server.web.page.admin.user.UserListPage;
import io.onedev.server.web.page.admin.user.UserMembershipsPage;
import io.onedev.server.web.page.admin.user.UserPasswordPage;
import io.onedev.server.web.page.admin.user.UserProfilePage;
import io.onedev.server.web.page.admin.user.UserTokenPage;
import io.onedev.server.web.page.init.ServerInitPage;
import io.onedev.server.web.page.my.MyAvatarPage;
import io.onedev.server.web.page.my.MyPasswordPage;
import io.onedev.server.web.page.my.MyProfilePage;
import io.onedev.server.web.page.my.MyTokenPage;
import io.onedev.server.web.page.project.NewProjectPage;
import io.onedev.server.web.page.project.NoCommitsPage;
import io.onedev.server.web.page.project.ProjectListPage;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.page.project.branches.ProjectBranchesPage;
import io.onedev.server.web.page.project.builds.ProjectBuildsPage;
import io.onedev.server.web.page.project.builds.detail.BuildChangesPage;
import io.onedev.server.web.page.project.builds.detail.BuildLogPage;
import io.onedev.server.web.page.project.builds.detail.FixedIssuesPage;
import io.onedev.server.web.page.project.codecomments.ProjectCodeCommentsPage;
import io.onedev.server.web.page.project.commits.CommitDetailPage;
import io.onedev.server.web.page.project.commits.ProjectCommitsPage;
import io.onedev.server.web.page.project.compare.RevisionComparePage;
import io.onedev.server.web.page.project.issues.boards.IssueBoardsPage;
import io.onedev.server.web.page.project.issues.create.NewIssuePage;
import io.onedev.server.web.page.project.issues.detail.IssueActivitiesPage;
import io.onedev.server.web.page.project.issues.list.IssueListPage;
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
import io.onedev.server.web.page.project.setting.branchprotection.BranchProtectionPage;
import io.onedev.server.web.page.project.setting.commitmessagetransform.CommitMessageTransformPage;
import io.onedev.server.web.page.project.setting.general.GeneralSettingPage;
import io.onedev.server.web.page.project.setting.issue.PromptFieldsUponIssueOpenSettingPage;
import io.onedev.server.web.page.project.setting.issue.StateTransitionsPage;
import io.onedev.server.web.page.project.setting.tagprotection.TagProtectionPage;
import io.onedev.server.web.page.project.setting.webhook.WebHooksPage;
import io.onedev.server.web.page.project.stats.ProjectContribsPage;
import io.onedev.server.web.page.project.stats.SourceLinesPage;
import io.onedev.server.web.page.project.tags.ProjectTagsPage;
import io.onedev.server.web.page.security.ForgetPage;
import io.onedev.server.web.page.security.LoginPage;
import io.onedev.server.web.page.security.LogoutPage;
import io.onedev.server.web.page.security.RegisterPage;
import io.onedev.server.web.stream.ArchiveStreamResourceReference;
import io.onedev.server.web.stream.AttachmentStreamResourceReference;
import io.onedev.server.web.stream.BuildLogStreamResourceReference;
import io.onedev.server.web.stream.RawBlobStreamResourceReference;
import io.onedev.server.web.stream.ServerLogStreamResourceReference;
import io.onedev.server.web.util.mapper.OnePageMapper;
import io.onedev.server.web.util.mapper.OneResourceMapper;

public class OneUrlMapper extends CompoundRequestMapper {

	public OneUrlMapper(WebApplication app) {
		add(new OnePageMapper("init", ServerInitPage.class));
		add(new OnePageMapper("loading", BrowserInfoPage.class));
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
		add(new OnePageMapper("my/token", MyTokenPage.class));
	}

	private void addResources() {
		add(new ResourceMapper("server-log-stream", new ServerLogStreamResourceReference()));
		add(new ResourceMapper("projects/${project}/archive/${revision}", new ArchiveStreamResourceReference()));
		add(new ResourceMapper("projects/${project}/builds/${build}/log-stream", new BuildLogStreamResourceReference()));
		
		add(new OneResourceMapper("projects/${project}/raw/${revision}/${path}", new RawBlobStreamResourceReference()));
		add(new ResourceMapper("projects/${project}/attachment/${uuid}/${attachment}", 
				new AttachmentStreamResourceReference()));
	}
	
	private void addSecurityPages() {
		add(new OnePageMapper("login", LoginPage.class));
		add(new OnePageMapper("logout", LogoutPage.class));
		add(new OnePageMapper("register", RegisterPage.class));
		add(new OnePageMapper("forget", ForgetPage.class));
	}
	
	private void addAdministrationPages() {
		add(new OnePageMapper("administration/users", UserListPage.class));
		add(new OnePageMapper("administration/users/new", NewUserPage.class));
		add(new OnePageMapper("administration/users/${user}/profile", UserProfilePage.class));
		add(new OnePageMapper("administration/users/${user}/groups", UserMembershipsPage.class));
		add(new OnePageMapper("administration/users/${user}/authorizations", UserAuthorizationsPage.class));
		add(new OnePageMapper("administration/users/${user}/avatar", UserAvatarPage.class));
		add(new OnePageMapper("administration/users/${user}/password", UserPasswordPage.class));
		add(new OnePageMapper("administration/users/${user}/token", UserTokenPage.class));
		
		add(new OnePageMapper("administration/groups", GroupListPage.class));
		add(new OnePageMapper("administration/groups/new", NewGroupPage.class));
		add(new OnePageMapper("administration/groups/${group}/profile", GroupProfilePage.class));
		add(new OnePageMapper("administration/groups/${group}/members", GroupMembershipsPage.class));
		add(new OnePageMapper("administration/groups/${group}/authorizations", GroupAuthorizationsPage.class));
		
		add(new OnePageMapper("administration/settings/system", SystemSettingPage.class));
		add(new OnePageMapper("administration/settings/mail", MailSettingPage.class));
		add(new OnePageMapper("administration/settings/backup", DatabaseBackupPage.class));
		add(new OnePageMapper("administration/settings/security", SecuritySettingPage.class));
		add(new OnePageMapper("administration/settings/authenticator", AuthenticatorPage.class));

		add(new OnePageMapper("administration/settings/job-executors", JobExecutorPage.class));
		
		add(new OnePageMapper("administration/settings/issue-fields", IssueFieldListPage.class));
		add(new OnePageMapper("administration/settings/issue-states", IssueStateListPage.class));
		add(new OnePageMapper("administration/settings/state-transitions", DefaultStateTransitionsPage.class));
		add(new OnePageMapper("administration/settings/issue-boards", DefaultBoardListPage.class));
		add(new OnePageMapper("administration/settings/default-issue-queries", DefaultQueryListPage.class));
		
		add(new OnePageMapper("administration/server-log", ServerLogPage.class));
		add(new OnePageMapper("administration/server-information", ServerInformationPage.class));
	}
	
	private void addProjectPages() {
		add(new OnePageMapper("projects", ProjectListPage.class) {
			
			/*
			 * This logic is added to prevent url "/" from being redirected to "/projects"
			 */
			@Override
			public Url mapHandler(IRequestHandler requestHandler) {
				if (requestHandler instanceof BookmarkablePageRequestHandler 
						|| requestHandler instanceof RenderPageRequestHandler) {
					IPageClassRequestHandler pageClassRequestHandler = (IPageClassRequestHandler) requestHandler;
					if (pageClassRequestHandler.getPageClass() == ProjectListPage.class) {
						return null;
					}
				}
				return super.mapHandler(requestHandler);
			}
			
		});
		add(new OnePageMapper("projects/new", NewProjectPage.class));
		add(new OnePageMapper("projects/${project}", ProjectBlobPage.class));

		add(new OnePageMapper("projects/${project}/blob/#{revision}/#{path}", ProjectBlobPage.class) {
			
			/*
			 * This logic is added to prevent url "/projects/<project>" from being redirected to 
			 * "/projects/<project>/blob"
			 */
			@Override
			public Url mapHandler(IRequestHandler requestHandler) {
				if (requestHandler instanceof BookmarkablePageRequestHandler 
						|| requestHandler instanceof RenderPageRequestHandler) {
					IPageClassRequestHandler pageClassRequestHandler = (IPageClassRequestHandler) requestHandler;
					if (pageClassRequestHandler.getPageClass() == ProjectBlobPage.class 
							&& pageClassRequestHandler.getPageParameters().get("revision").toString() == null) {
						return null;
					}
				}
				return super.mapHandler(requestHandler);
			}
			
		});
		add(new OnePageMapper("projects/${project}/commits/${revision}", CommitDetailPage.class));
		add(new OnePageMapper("projects/${project}/commits", ProjectCommitsPage.class));
		add(new OnePageMapper("projects/${project}/compare", RevisionComparePage.class));
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

		add(new OnePageMapper("projects/${project}/issue-boards", IssueBoardsPage.class));
		add(new OnePageMapper("projects/${project}/issue-boards/${board}", IssueBoardsPage.class));
		add(new OnePageMapper("projects/${project}/issue-list", IssueListPage.class));
		add(new OnePageMapper("projects/${project}/issues/${issue}/activities", IssueActivitiesPage.class));
		add(new OnePageMapper("projects/${project}/issues/new", NewIssuePage.class));
		add(new OnePageMapper("projects/${project}/builds", ProjectBuildsPage.class));
		add(new OnePageMapper("projects/${project}/builds/${build}", BuildLogPage.class));
		add(new OnePageMapper("projects/${project}/builds/${build}/log", BuildLogPage.class));
		add(new OnePageMapper("projects/${project}/builds/${build}/changes", BuildChangesPage.class));
		add(new OnePageMapper("projects/${project}/builds/${build}/fixed-issues", FixedIssuesPage.class));
		add(new OnePageMapper("projects/${project}/milestones", MilestoneListPage.class));
		add(new OnePageMapper("projects/${project}/milestones/${milestone}", MilestoneDetailPage.class));
		add(new OnePageMapper("projects/${project}/milestones/${milestone}/edit", MilestoneEditPage.class));
		add(new OnePageMapper("projects/${project}/milestones/new", NewMilestonePage.class));
		
		add(new OnePageMapper("projects/${project}/settings/general", GeneralSettingPage.class));
		add(new OnePageMapper("projects/${project}/settings/authorizations", ProjectAuthorizationsPage.class));
		add(new OnePageMapper("projects/${project}/settings/avatar-edit", AvatarEditPage.class));
		add(new OnePageMapper("projects/${project}/settings/branch-protection", BranchProtectionPage.class));
		add(new OnePageMapper("projects/${project}/settings/tag-protection", TagProtectionPage.class));
		add(new OnePageMapper("projects/${project}/settings/issue/state-transitions", StateTransitionsPage.class));
		add(new OnePageMapper("projects/${project}/settings/issue/prompt-fields-upon-issue-open", 
				PromptFieldsUponIssueOpenSettingPage.class));
		add(new OnePageMapper("projects/${project}/settings/commit-message-transform", CommitMessageTransformPage.class));
		add(new OnePageMapper("projects/${project}/settings/web-hooks", WebHooksPage.class));
		
		add(new OnePageMapper("projects/${project}/no-commits", NoCommitsPage.class));
	}

}
