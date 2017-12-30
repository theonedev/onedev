package com.gitplex.server.web;

import org.apache.wicket.core.request.handler.BookmarkablePageRequestHandler;
import org.apache.wicket.core.request.handler.IPageClassRequestHandler;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler;
import org.apache.wicket.core.request.mapper.ResourceMapper;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.mapper.CompoundRequestMapper;

import com.gitplex.server.web.page.admin.authenticator.AuthenticatorPage;
import com.gitplex.server.web.page.admin.databasebackup.DatabaseBackupPage;
import com.gitplex.server.web.page.admin.licensemanagement.LicenseManagementPage;
import com.gitplex.server.web.page.admin.mailsetting.MailSettingPage;
import com.gitplex.server.web.page.admin.securitysetting.SecuritySettingPage;
import com.gitplex.server.web.page.admin.serverinformation.ServerInformationPage;
import com.gitplex.server.web.page.admin.serverlog.ServerLogPage;
import com.gitplex.server.web.page.admin.systemsetting.SystemSettingPage;
import com.gitplex.server.web.page.group.GroupAuthorizationsPage;
import com.gitplex.server.web.page.group.GroupListPage;
import com.gitplex.server.web.page.group.GroupMembershipsPage;
import com.gitplex.server.web.page.group.GroupProfilePage;
import com.gitplex.server.web.page.group.NewGroupPage;
import com.gitplex.server.web.page.init.ServerInitPage;
import com.gitplex.server.web.page.project.NewProjectPage;
import com.gitplex.server.web.page.project.NoCommitsPage;
import com.gitplex.server.web.page.project.ProjectListPage;
import com.gitplex.server.web.page.project.blob.ProjectBlobPage;
import com.gitplex.server.web.page.project.branches.ProjectBranchesPage;
import com.gitplex.server.web.page.project.comments.ProjectCodeCommentsPage;
import com.gitplex.server.web.page.project.commit.CommitDetailPage;
import com.gitplex.server.web.page.project.commit.ProjectCommitsPage;
import com.gitplex.server.web.page.project.compare.RevisionComparePage;
import com.gitplex.server.web.page.project.pullrequest.InvalidRequestPage;
import com.gitplex.server.web.page.project.pullrequest.newrequest.NewRequestPage;
import com.gitplex.server.web.page.project.pullrequest.requestdetail.changes.RequestChangesPage;
import com.gitplex.server.web.page.project.pullrequest.requestdetail.codecomments.RequestCodeCommentsPage;
import com.gitplex.server.web.page.project.pullrequest.requestdetail.mergepreview.MergePreviewPage;
import com.gitplex.server.web.page.project.pullrequest.requestdetail.overview.RequestOverviewPage;
import com.gitplex.server.web.page.project.pullrequest.requestlist.RequestListPage;
import com.gitplex.server.web.page.project.setting.authorization.ProjectAuthorizationsPage;
import com.gitplex.server.web.page.project.setting.branchprotection.BranchProtectionPage;
import com.gitplex.server.web.page.project.setting.commitmessagetransform.CommitMessageTransformPage;
import com.gitplex.server.web.page.project.setting.general.GeneralSettingPage;
import com.gitplex.server.web.page.project.setting.tagprotection.TagProtectionPage;
import com.gitplex.server.web.page.project.stats.ProjectStatsPage;
import com.gitplex.server.web.page.project.tags.ProjectTagsPage;
import com.gitplex.server.web.page.security.ForgetPage;
import com.gitplex.server.web.page.security.LoginPage;
import com.gitplex.server.web.page.security.LogoutPage;
import com.gitplex.server.web.page.security.RegisterPage;
import com.gitplex.server.web.page.test.TestPage;
import com.gitplex.server.web.page.user.AvatarEditPage;
import com.gitplex.server.web.page.user.NewUserPage;
import com.gitplex.server.web.page.user.PasswordEditPage;
import com.gitplex.server.web.page.user.TaskListPage;
import com.gitplex.server.web.page.user.TokenGeneratePage;
import com.gitplex.server.web.page.user.UserAuthorizationsPage;
import com.gitplex.server.web.page.user.UserListPage;
import com.gitplex.server.web.page.user.UserMembershipsPage;
import com.gitplex.server.web.page.user.UserProfilePage;
import com.gitplex.server.web.util.mapper.GitPlexPageMapper;
import com.gitplex.server.web.util.mapper.GitPlexResourceMapper;
import com.gitplex.server.web.util.resource.ArchiveResourceReference;
import com.gitplex.server.web.util.resource.AttachmentResourceReference;
import com.gitplex.server.web.util.resource.RawBlobResourceReference;
import com.gitplex.server.web.util.resource.ServerLogResourceReference;

public class GitPlexUrlMapper extends CompoundRequestMapper {

	public GitPlexUrlMapper(WebApplication app) {
		add(new GitPlexPageMapper("init", ServerInitPage.class));
		addAdministrationPages();
		addUserPages();
		addGroupPages();
		addProjectPages();
		addSecurityPages();
		
		add(new GitPlexPageMapper("test", TestPage.class));
		
		addResources();
	}

	private void addResources() {
		add(new ResourceMapper("resource/server-log", new ServerLogResourceReference()));
		add(new ResourceMapper("projects/${project}/archive/${revision}", new ArchiveResourceReference()));
		add(new GitPlexResourceMapper("projects/${project}/raw/${revision}/${path}", new RawBlobResourceReference()));
		add(new ResourceMapper("projects/${project}/attachment/${uuid}/${attachment}", 
				new AttachmentResourceReference()));
	}
	
	private void addSecurityPages() {
		add(new GitPlexPageMapper("login", LoginPage.class));
		add(new GitPlexPageMapper("logout", LogoutPage.class));
		add(new GitPlexPageMapper("register", RegisterPage.class));
		add(new GitPlexPageMapper("forget", ForgetPage.class));
	}
	
	private void addAdministrationPages() {
		add(new GitPlexPageMapper("administration/settings/system", SystemSettingPage.class));
		add(new GitPlexPageMapper("administration/settings/mail", MailSettingPage.class));
		add(new GitPlexPageMapper("administration/settings/backup", DatabaseBackupPage.class));
		add(new GitPlexPageMapper("administration/settings/security", SecuritySettingPage.class));
		add(new GitPlexPageMapper("administration/settings/authenticator", AuthenticatorPage.class));
		add(new GitPlexPageMapper("administration/server-log", ServerLogPage.class));
		add(new GitPlexPageMapper("administration/server-information", ServerInformationPage.class));
		add(new GitPlexPageMapper("administration/license-management", LicenseManagementPage.class));
	}
	
	private void addUserPages() {
		add(new GitPlexPageMapper("users", UserListPage.class));
		add(new GitPlexPageMapper("users/new", NewUserPage.class));
		add(new GitPlexPageMapper("users/${user}/profile", UserProfilePage.class));
		add(new GitPlexPageMapper("users/${user}/groups", UserMembershipsPage.class));
		add(new GitPlexPageMapper("users/${user}/authorizations", UserAuthorizationsPage.class));
		add(new GitPlexPageMapper("users/${user}/tasks", TaskListPage.class));
		add(new GitPlexPageMapper("users/${user}/avatar", AvatarEditPage.class));
		add(new GitPlexPageMapper("users/${user}/password", PasswordEditPage.class));
		add(new GitPlexPageMapper("users/${user}/token", TokenGeneratePage.class));
	}

	private void addGroupPages() {
		add(new GitPlexPageMapper("groups", GroupListPage.class));
		add(new GitPlexPageMapper("groups/new", NewGroupPage.class));
		add(new GitPlexPageMapper("groups/${group}/profile", GroupProfilePage.class));
		add(new GitPlexPageMapper("groups/${group}/members", GroupMembershipsPage.class));
		add(new GitPlexPageMapper("groups/${group}/authorizations", GroupAuthorizationsPage.class));
	}
	
	private void addProjectPages() {
		add(new GitPlexPageMapper("projects", ProjectListPage.class));
		add(new GitPlexPageMapper("projects/new", NewProjectPage.class));
		add(new GitPlexPageMapper("projects/${project}", ProjectBlobPage.class));

		add(new GitPlexPageMapper("projects/${project}/blob/#{revision}/#{path}", ProjectBlobPage.class) {
			
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
		add(new GitPlexPageMapper("projects/${project}/commit/${revision}", CommitDetailPage.class));
		add(new GitPlexPageMapper("projects/${project}/commits", ProjectCommitsPage.class));
		add(new GitPlexPageMapper("projects/${project}/compare", RevisionComparePage.class));
		add(new GitPlexPageMapper("projects/${project}/stats", ProjectStatsPage.class));

		add(new GitPlexPageMapper("projects/${project}/branches", ProjectBranchesPage.class));
		add(new GitPlexPageMapper("projects/${project}/tags", ProjectTagsPage.class));
		add(new GitPlexPageMapper("projects/${project}/codecomments", ProjectCodeCommentsPage.class));

		add(new GitPlexPageMapper("projects/${project}/pull", RequestListPage.class));
		add(new GitPlexPageMapper("projects/${project}/pull/new", NewRequestPage.class));
		add(new GitPlexPageMapper("projects/${project}/pull/${request}", RequestOverviewPage.class));
		add(new GitPlexPageMapper("projects/${project}/pull/${request}/overview", RequestOverviewPage.class));
		add(new GitPlexPageMapper("projects/${project}/pull/${request}/code-comments", RequestCodeCommentsPage.class));
		add(new GitPlexPageMapper("projects/${project}/pull/${request}/changes", RequestChangesPage.class));
		add(new GitPlexPageMapper("projects/${project}/pull/${request}/merge-preview", MergePreviewPage.class));
		add(new GitPlexPageMapper("projects/${project}/pull/${request}/invalid", InvalidRequestPage.class));

		add(new GitPlexPageMapper("projects/${project}/settings/general", GeneralSettingPage.class));
		add(new GitPlexPageMapper("projects/${project}/settings/authorizations", ProjectAuthorizationsPage.class));
		add(new GitPlexPageMapper("projects/${project}/settings/branch-protection", BranchProtectionPage.class));
		add(new GitPlexPageMapper("projects/${project}/settings/tag-protection", TagProtectionPage.class));
		add(new GitPlexPageMapper("projects/${project}/settings/commit-message-transform", CommitMessageTransformPage.class));
		
		add(new GitPlexPageMapper("projects/${project}/no-commits", NoCommitsPage.class));
	}

}
