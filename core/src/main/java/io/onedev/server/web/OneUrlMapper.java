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
import io.onedev.server.web.page.group.GroupAuthorizationsPage;
import io.onedev.server.web.page.group.GroupListPage;
import io.onedev.server.web.page.group.GroupMembershipsPage;
import io.onedev.server.web.page.group.GroupProfilePage;
import io.onedev.server.web.page.group.NewGroupPage;
import io.onedev.server.web.page.init.ServerInitPage;
import io.onedev.server.web.page.project.NewProjectPage;
import io.onedev.server.web.page.project.NoCommitsPage;
import io.onedev.server.web.page.project.ProjectListPage;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.page.project.branches.ProjectBranchesPage;
import io.onedev.server.web.page.project.comments.ProjectCodeCommentsPage;
import io.onedev.server.web.page.project.commits.CommitDetailPage;
import io.onedev.server.web.page.project.commits.ProjectCommitsPage;
import io.onedev.server.web.page.project.compare.RevisionComparePage;
import io.onedev.server.web.page.project.issues.issuedetail.overview.IssueOverviewPage;
import io.onedev.server.web.page.project.issues.issuelist.IssueListPage;
import io.onedev.server.web.page.project.issues.newissue.NewIssuePage;
import io.onedev.server.web.page.project.pullrequests.InvalidRequestPage;
import io.onedev.server.web.page.project.pullrequests.newrequest.NewRequestPage;
import io.onedev.server.web.page.project.pullrequests.requestdetail.changes.RequestChangesPage;
import io.onedev.server.web.page.project.pullrequests.requestdetail.codecomments.RequestCodeCommentsPage;
import io.onedev.server.web.page.project.pullrequests.requestdetail.mergepreview.MergePreviewPage;
import io.onedev.server.web.page.project.pullrequests.requestdetail.overview.RequestOverviewPage;
import io.onedev.server.web.page.project.pullrequests.requestlist.RequestListPage;
import io.onedev.server.web.page.project.setting.authorization.ProjectAuthorizationsPage;
import io.onedev.server.web.page.project.setting.branchprotection.BranchProtectionPage;
import io.onedev.server.web.page.project.setting.commitmessagetransform.CommitMessageTransformPage;
import io.onedev.server.web.page.project.setting.general.GeneralSettingPage;
import io.onedev.server.web.page.project.setting.issueworkflow.fields.IssueFieldsPage;
import io.onedev.server.web.page.project.setting.issueworkflow.states.IssueStatesPage;
import io.onedev.server.web.page.project.setting.issueworkflow.statetransitions.StateTransitionsPage;
import io.onedev.server.web.page.project.setting.tagprotection.TagProtectionPage;
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
import io.onedev.server.web.page.user.PasswordEditPage;
import io.onedev.server.web.page.user.TaskListPage;
import io.onedev.server.web.page.user.TokenGeneratePage;
import io.onedev.server.web.page.user.UserAuthorizationsPage;
import io.onedev.server.web.page.user.UserListPage;
import io.onedev.server.web.page.user.UserMembershipsPage;
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
		addGroupPages();
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
		add(new OnePageMapper("users/${user}/groups", UserMembershipsPage.class));
		add(new OnePageMapper("users/${user}/authorizations", UserAuthorizationsPage.class));
		add(new OnePageMapper("users/${user}/tasks", TaskListPage.class));
		add(new OnePageMapper("users/${user}/avatar", AvatarEditPage.class));
		add(new OnePageMapper("users/${user}/password", PasswordEditPage.class));
		add(new OnePageMapper("users/${user}/token", TokenGeneratePage.class));
	}

	private void addGroupPages() {
		add(new OnePageMapper("groups", GroupListPage.class));
		add(new OnePageMapper("groups/new", NewGroupPage.class));
		add(new OnePageMapper("groups/${group}/profile", GroupProfilePage.class));
		add(new OnePageMapper("groups/${group}/members", GroupMembershipsPage.class));
		add(new OnePageMapper("groups/${group}/authorizations", GroupAuthorizationsPage.class));
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

		add(new OnePageMapper("projects/${project}/pulls", RequestListPage.class));
		add(new OnePageMapper("projects/${project}/pulls/new", NewRequestPage.class));
		add(new OnePageMapper("projects/${project}/pulls/${request}", RequestOverviewPage.class));
		add(new OnePageMapper("projects/${project}/pulls/${request}/overview", RequestOverviewPage.class));
		add(new OnePageMapper("projects/${project}/pulls/${request}/code-comments", RequestCodeCommentsPage.class));
		add(new OnePageMapper("projects/${project}/pulls/${request}/changes", RequestChangesPage.class));
		add(new OnePageMapper("projects/${project}/pulls/${request}/merge-preview", MergePreviewPage.class));
		add(new OnePageMapper("projects/${project}/pulls/${request}/invalid", InvalidRequestPage.class));

		add(new OnePageMapper("projects/${project}/issues", IssueListPage.class));
		add(new OnePageMapper("projects/${project}/issues/${issue}/overview", IssueOverviewPage.class));
		add(new OnePageMapper("projects/${project}/issues/new", NewIssuePage.class));
		
		add(new OnePageMapper("projects/${project}/settings/general", GeneralSettingPage.class));
		add(new OnePageMapper("projects/${project}/settings/authorizations", ProjectAuthorizationsPage.class));
		add(new OnePageMapper("projects/${project}/settings/branch-protection", BranchProtectionPage.class));
		add(new OnePageMapper("projects/${project}/settings/tag-protection", TagProtectionPage.class));
		add(new OnePageMapper("projects/${project}/settings/commit-message-transform", CommitMessageTransformPage.class));
		add(new OnePageMapper("projects/${project}/settings/issue-fields", IssueFieldsPage.class));
		add(new OnePageMapper("projects/${project}/settings/issue-states", IssueStatesPage.class));
		add(new OnePageMapper("projects/${project}/settings/issue-state-transitions", StateTransitionsPage.class));
		
		add(new OnePageMapper("projects/${project}/no-commits", NoCommitsPage.class));
	}

}
