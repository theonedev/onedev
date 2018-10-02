package io.onedev.server.web;

import org.apache.wicket.core.request.handler.BookmarkablePageRequestHandler;
import org.apache.wicket.core.request.handler.IPageClassRequestHandler;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler;
import org.apache.wicket.core.request.mapper.ResourceMapper;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.mapper.CompoundRequestMapper;

import io.onedev.server.web.page.admin.authenticator.AuthenticatorPage;
import io.onedev.server.web.page.admin.databasebackup.DatabaseBackupPage;
import io.onedev.server.web.page.admin.licensemanagement.LicenseManagementPage;
import io.onedev.server.web.page.admin.mailsetting.MailSettingPage;
import io.onedev.server.web.page.admin.securitysetting.SecuritySettingPage;
import io.onedev.server.web.page.admin.serverinformation.ServerInformationPage;
import io.onedev.server.web.page.admin.serverlog.ServerLogPage;
import io.onedev.server.web.page.admin.systemsetting.SystemSettingPage;
import io.onedev.server.web.page.init.ServerInitPage;
import io.onedev.server.web.page.project.NewProjectPage;
import io.onedev.server.web.page.project.NoCommitsPage;
import io.onedev.server.web.page.project.ProjectListPage;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.page.project.branches.ProjectBranchesPage;
import io.onedev.server.web.page.project.builds.BuildListPage;
import io.onedev.server.web.page.project.comments.ProjectCodeCommentsPage;
import io.onedev.server.web.page.project.commits.CommitDetailPage;
import io.onedev.server.web.page.project.commits.ProjectCommitsPage;
import io.onedev.server.web.page.project.compare.RevisionComparePage;
import io.onedev.server.web.page.project.issues.issueboards.IssueBoardsPage;
import io.onedev.server.web.page.project.issues.issuedetail.IssueActivitiesPage;
import io.onedev.server.web.page.project.issues.issuedetail.IssueBuildsPage;
import io.onedev.server.web.page.project.issues.issuedetail.IssueCommitsPage;
import io.onedev.server.web.page.project.issues.issuedetail.IssuePullRequestsPage;
import io.onedev.server.web.page.project.issues.issuelist.IssueListPage;
import io.onedev.server.web.page.project.issues.milestones.MilestoneDetailPage;
import io.onedev.server.web.page.project.issues.milestones.MilestoneEditPage;
import io.onedev.server.web.page.project.issues.milestones.MilestoneListPage;
import io.onedev.server.web.page.project.issues.milestones.NewMilestonePage;
import io.onedev.server.web.page.project.issues.newissue.NewIssuePage;
import io.onedev.server.web.page.project.pullrequests.InvalidRequestPage;
import io.onedev.server.web.page.project.pullrequests.create.NewPullRequestPage;
import io.onedev.server.web.page.project.pullrequests.detail.activities.PullRequestActivitiesPage;
import io.onedev.server.web.page.project.pullrequests.detail.changes.PullRequestChangesPage;
import io.onedev.server.web.page.project.pullrequests.detail.codecomments.PullRequestCodeCommentsPage;
import io.onedev.server.web.page.project.pullrequests.detail.mergepreview.MergePreviewPage;
import io.onedev.server.web.page.project.pullrequests.list.PullRequestListPage;
import io.onedev.server.web.page.project.setting.branchprotection.BranchProtectionPage;
import io.onedev.server.web.page.project.setting.commitmessagetransform.CommitMessageTransformPage;
import io.onedev.server.web.page.project.setting.configuration.ConfigurationEditPage;
import io.onedev.server.web.page.project.setting.configuration.ConfigurationListPage;
import io.onedev.server.web.page.project.setting.configuration.NewConfigurationPage;
import io.onedev.server.web.page.project.setting.general.GeneralSettingPage;
import io.onedev.server.web.page.project.setting.issueworkflow.fields.IssueFieldsPage;
import io.onedev.server.web.page.project.setting.issueworkflow.states.IssueStatesPage;
import io.onedev.server.web.page.project.setting.issueworkflow.statetransitions.StateTransitionsPage;
import io.onedev.server.web.page.project.setting.tagprotection.TagProtectionPage;
import io.onedev.server.web.page.project.setting.team.NewTeamPage;
import io.onedev.server.web.page.project.setting.team.TeamEditPage;
import io.onedev.server.web.page.project.setting.team.TeamListPage;
import io.onedev.server.web.page.project.setting.webhook.WebHooksPage;
import io.onedev.server.web.page.project.stats.ProjectContribsPage;
import io.onedev.server.web.page.project.stats.SourceLinesPage;
import io.onedev.server.web.page.project.tags.ProjectTagsPage;
import io.onedev.server.web.page.security.ForgetPage;
import io.onedev.server.web.page.security.LoginPage;
import io.onedev.server.web.page.security.LogoutPage;
import io.onedev.server.web.page.security.RegisterPage;
import io.onedev.server.web.page.test.TestPage;
import io.onedev.server.web.page.user.AvatarEditPage;
import io.onedev.server.web.page.user.NewUserPage;
import io.onedev.server.web.page.user.ParticipatedProjectsPage;
import io.onedev.server.web.page.user.PasswordEditPage;
import io.onedev.server.web.page.user.TokenGeneratePage;
import io.onedev.server.web.page.user.UserListPage;
import io.onedev.server.web.page.user.UserProfilePage;
import io.onedev.server.web.util.mapper.OnePageMapper;
import io.onedev.server.web.util.mapper.OneResourceMapper;
import io.onedev.server.web.util.resource.ArchiveResourceReference;
import io.onedev.server.web.util.resource.AttachmentResourceReference;
import io.onedev.server.web.util.resource.RawBlobResourceReference;
import io.onedev.server.web.util.resource.ServerLogResourceReference;

public class OneUrlMapper extends CompoundRequestMapper {

	public OneUrlMapper(WebApplication app) {
		add(new OnePageMapper("init", ServerInitPage.class));
		addAdministrationPages();
		addUserPages();
		addProjectPages();
		addSecurityPages();
		
		add(new OnePageMapper("test", TestPage.class));
		
		addResources();
	}

	private void addResources() {
		add(new ResourceMapper("resource/server-log", new ServerLogResourceReference()));
		add(new ResourceMapper("projects/${project}/archive/${revision}", new ArchiveResourceReference()));
		add(new OneResourceMapper("projects/${project}/raw/${revision}/${path}", new RawBlobResourceReference()));
		add(new ResourceMapper("projects/${project}/attachment/${uuid}/${attachment}", 
				new AttachmentResourceReference()));
	}
	
	private void addSecurityPages() {
		add(new OnePageMapper("login", LoginPage.class));
		add(new OnePageMapper("logout", LogoutPage.class));
		add(new OnePageMapper("register", RegisterPage.class));
		add(new OnePageMapper("forget", ForgetPage.class));
	}
	
	private void addAdministrationPages() {
		add(new OnePageMapper("administration/settings/system", SystemSettingPage.class));
		add(new OnePageMapper("administration/settings/mail", MailSettingPage.class));
		add(new OnePageMapper("administration/settings/backup", DatabaseBackupPage.class));
		add(new OnePageMapper("administration/settings/security", SecuritySettingPage.class));
		add(new OnePageMapper("administration/settings/authenticator", AuthenticatorPage.class));
		add(new OnePageMapper("administration/server-log", ServerLogPage.class));
		add(new OnePageMapper("administration/server-information", ServerInformationPage.class));
		add(new OnePageMapper("administration/license-management", LicenseManagementPage.class));
	}
	
	private void addUserPages() {
		add(new OnePageMapper("users", UserListPage.class));
		add(new OnePageMapper("users/new", NewUserPage.class));
		add(new OnePageMapper("users/${user}/profile", UserProfilePage.class));
		add(new OnePageMapper("users/${user}/avatar", AvatarEditPage.class));
		add(new OnePageMapper("users/${user}/password", PasswordEditPage.class));
		add(new OnePageMapper("users/${user}/token", TokenGeneratePage.class));
		add(new OnePageMapper("users/${user}/participated-projects", ParticipatedProjectsPage.class));
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

		add(new OnePageMapper("projects/${project}/pulls", PullRequestListPage.class));
		add(new OnePageMapper("projects/${project}/pulls/new", NewPullRequestPage.class));
		add(new OnePageMapper("projects/${project}/pulls/${request}", PullRequestActivitiesPage.class));
		add(new OnePageMapper("projects/${project}/pulls/${request}/activities", PullRequestActivitiesPage.class));
		add(new OnePageMapper("projects/${project}/pulls/${request}/code-comments", PullRequestCodeCommentsPage.class));
		add(new OnePageMapper("projects/${project}/pulls/${request}/changes", PullRequestChangesPage.class));
		add(new OnePageMapper("projects/${project}/pulls/${request}/merge-preview", MergePreviewPage.class));
		add(new OnePageMapper("projects/${project}/pulls/${request}/invalid", InvalidRequestPage.class));

		add(new OnePageMapper("projects/${project}/issue-boards", IssueBoardsPage.class));
		add(new OnePageMapper("projects/${project}/issue-boards/${board}", IssueBoardsPage.class));
		add(new OnePageMapper("projects/${project}/issue-list", IssueListPage.class));
		add(new OnePageMapper("projects/${project}/issues/${issue}/activities", IssueActivitiesPage.class));
		add(new OnePageMapper("projects/${project}/issues/${issue}/commits", IssueCommitsPage.class));
		add(new OnePageMapper("projects/${project}/issues/${issue}/builds", IssueBuildsPage.class));
		add(new OnePageMapper("projects/${project}/issues/${issue}/pulls", IssuePullRequestsPage.class));
		add(new OnePageMapper("projects/${project}/issues/new", NewIssuePage.class));
		add(new OnePageMapper("projects/${project}/milestones", MilestoneListPage.class));
		add(new OnePageMapper("projects/${project}/milestones/${milestone}", MilestoneDetailPage.class));
		add(new OnePageMapper("projects/${project}/milestones/${milestone}/edit", MilestoneEditPage.class));
		add(new OnePageMapper("projects/${project}/milestones/new", NewMilestonePage.class));
		
		add(new OnePageMapper("projects/${project}/builds", BuildListPage.class));
		
		add(new OnePageMapper("projects/${project}/settings/general", GeneralSettingPage.class));
		add(new OnePageMapper("projects/${project}/settings/teams", TeamListPage.class));
		add(new OnePageMapper("projects/${project}/settings/teams/new", NewTeamPage.class));
		add(new OnePageMapper("projects/${project}/settings/teams/${team}", TeamEditPage.class));
		add(new OnePageMapper("projects/${project}/settings/branch-protection", BranchProtectionPage.class));
		add(new OnePageMapper("projects/${project}/settings/tag-protection", TagProtectionPage.class));
		add(new OnePageMapper("projects/${project}/settings/commit-message-transform", CommitMessageTransformPage.class));
		add(new OnePageMapper("projects/${project}/settings/issue-fields", IssueFieldsPage.class));
		add(new OnePageMapper("projects/${project}/settings/issue-states", IssueStatesPage.class));
		add(new OnePageMapper("projects/${project}/settings/issue-state-transitions", StateTransitionsPage.class));
		add(new OnePageMapper("projects/${project}/settings/configurations", ConfigurationListPage.class));
		add(new OnePageMapper("projects/${project}/settings/configurations/new", NewConfigurationPage.class));
		add(new OnePageMapper("projects/${project}/settings/configurations/${configuration}", ConfigurationEditPage.class));
		add(new OnePageMapper("projects/${project}/settings/web-hooks", WebHooksPage.class));
		
		add(new OnePageMapper("projects/${project}/no-commits", NoCommitsPage.class));
	}

}
