package com.turbodev.server.web;

import org.apache.wicket.core.request.handler.BookmarkablePageRequestHandler;
import org.apache.wicket.core.request.handler.IPageClassRequestHandler;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler;
import org.apache.wicket.core.request.mapper.ResourceMapper;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.mapper.CompoundRequestMapper;

import com.turbodev.server.web.page.admin.authenticator.AuthenticatorPage;
import com.turbodev.server.web.page.admin.databasebackup.DatabaseBackupPage;
import com.turbodev.server.web.page.admin.licensemanagement.LicenseManagementPage;
import com.turbodev.server.web.page.admin.mailsetting.MailSettingPage;
import com.turbodev.server.web.page.admin.securitysetting.SecuritySettingPage;
import com.turbodev.server.web.page.admin.serverinformation.ServerInformationPage;
import com.turbodev.server.web.page.admin.serverlog.ServerLogPage;
import com.turbodev.server.web.page.admin.systemsetting.SystemSettingPage;
import com.turbodev.server.web.page.group.GroupAuthorizationsPage;
import com.turbodev.server.web.page.group.GroupListPage;
import com.turbodev.server.web.page.group.GroupMembershipsPage;
import com.turbodev.server.web.page.group.GroupProfilePage;
import com.turbodev.server.web.page.group.NewGroupPage;
import com.turbodev.server.web.page.init.ServerInitPage;
import com.turbodev.server.web.page.project.NewProjectPage;
import com.turbodev.server.web.page.project.NoCommitsPage;
import com.turbodev.server.web.page.project.ProjectListPage;
import com.turbodev.server.web.page.project.blob.ProjectBlobPage;
import com.turbodev.server.web.page.project.branches.ProjectBranchesPage;
import com.turbodev.server.web.page.project.comments.ProjectCodeCommentsPage;
import com.turbodev.server.web.page.project.commit.CommitDetailPage;
import com.turbodev.server.web.page.project.commit.ProjectCommitsPage;
import com.turbodev.server.web.page.project.compare.RevisionComparePage;
import com.turbodev.server.web.page.project.pullrequest.InvalidRequestPage;
import com.turbodev.server.web.page.project.pullrequest.newrequest.NewRequestPage;
import com.turbodev.server.web.page.project.pullrequest.requestdetail.changes.RequestChangesPage;
import com.turbodev.server.web.page.project.pullrequest.requestdetail.codecomments.RequestCodeCommentsPage;
import com.turbodev.server.web.page.project.pullrequest.requestdetail.mergepreview.MergePreviewPage;
import com.turbodev.server.web.page.project.pullrequest.requestdetail.overview.RequestOverviewPage;
import com.turbodev.server.web.page.project.pullrequest.requestlist.RequestListPage;
import com.turbodev.server.web.page.project.setting.authorization.ProjectAuthorizationsPage;
import com.turbodev.server.web.page.project.setting.branchprotection.BranchProtectionPage;
import com.turbodev.server.web.page.project.setting.commitmessagetransform.CommitMessageTransformPage;
import com.turbodev.server.web.page.project.setting.general.GeneralSettingPage;
import com.turbodev.server.web.page.project.setting.tagprotection.TagProtectionPage;
import com.turbodev.server.web.page.project.stats.ProjectContribsPage;
import com.turbodev.server.web.page.project.stats.SourceLinesPage;
import com.turbodev.server.web.page.project.tags.ProjectTagsPage;
import com.turbodev.server.web.page.security.ForgetPage;
import com.turbodev.server.web.page.security.LoginPage;
import com.turbodev.server.web.page.security.LogoutPage;
import com.turbodev.server.web.page.security.RegisterPage;
import com.turbodev.server.web.page.test.TestPage;
import com.turbodev.server.web.page.user.AvatarEditPage;
import com.turbodev.server.web.page.user.NewUserPage;
import com.turbodev.server.web.page.user.PasswordEditPage;
import com.turbodev.server.web.page.user.TaskListPage;
import com.turbodev.server.web.page.user.TokenGeneratePage;
import com.turbodev.server.web.page.user.UserAuthorizationsPage;
import com.turbodev.server.web.page.user.UserListPage;
import com.turbodev.server.web.page.user.UserMembershipsPage;
import com.turbodev.server.web.page.user.UserProfilePage;
import com.turbodev.server.web.util.mapper.TurboDevPageMapper;
import com.turbodev.server.web.util.mapper.TurboDevResourceMapper;
import com.turbodev.server.web.util.resource.ArchiveResourceReference;
import com.turbodev.server.web.util.resource.AttachmentResourceReference;
import com.turbodev.server.web.util.resource.RawBlobResourceReference;
import com.turbodev.server.web.util.resource.ServerLogResourceReference;

public class TurboDevUrlMapper extends CompoundRequestMapper {

	public TurboDevUrlMapper(WebApplication app) {
		add(new TurboDevPageMapper("init", ServerInitPage.class));
		addAdministrationPages();
		addUserPages();
		addGroupPages();
		addProjectPages();
		addSecurityPages();
		
		add(new TurboDevPageMapper("test", TestPage.class));
		
		addResources();
	}

	private void addResources() {
		add(new ResourceMapper("resource/server-log", new ServerLogResourceReference()));
		add(new ResourceMapper("projects/${project}/archive/${revision}", new ArchiveResourceReference()));
		add(new TurboDevResourceMapper("projects/${project}/raw/${revision}/${path}", new RawBlobResourceReference()));
		add(new ResourceMapper("projects/${project}/attachment/${uuid}/${attachment}", 
				new AttachmentResourceReference()));
	}
	
	private void addSecurityPages() {
		add(new TurboDevPageMapper("login", LoginPage.class));
		add(new TurboDevPageMapper("logout", LogoutPage.class));
		add(new TurboDevPageMapper("register", RegisterPage.class));
		add(new TurboDevPageMapper("forget", ForgetPage.class));
	}
	
	private void addAdministrationPages() {
		add(new TurboDevPageMapper("administration/settings/system", SystemSettingPage.class));
		add(new TurboDevPageMapper("administration/settings/mail", MailSettingPage.class));
		add(new TurboDevPageMapper("administration/settings/backup", DatabaseBackupPage.class));
		add(new TurboDevPageMapper("administration/settings/security", SecuritySettingPage.class));
		add(new TurboDevPageMapper("administration/settings/authenticator", AuthenticatorPage.class));
		add(new TurboDevPageMapper("administration/server-log", ServerLogPage.class));
		add(new TurboDevPageMapper("administration/server-information", ServerInformationPage.class));
		add(new TurboDevPageMapper("administration/license-management", LicenseManagementPage.class));
	}
	
	private void addUserPages() {
		add(new TurboDevPageMapper("users", UserListPage.class));
		add(new TurboDevPageMapper("users/new", NewUserPage.class));
		add(new TurboDevPageMapper("users/${user}/profile", UserProfilePage.class));
		add(new TurboDevPageMapper("users/${user}/groups", UserMembershipsPage.class));
		add(new TurboDevPageMapper("users/${user}/authorizations", UserAuthorizationsPage.class));
		add(new TurboDevPageMapper("users/${user}/tasks", TaskListPage.class));
		add(new TurboDevPageMapper("users/${user}/avatar", AvatarEditPage.class));
		add(new TurboDevPageMapper("users/${user}/password", PasswordEditPage.class));
		add(new TurboDevPageMapper("users/${user}/token", TokenGeneratePage.class));
	}

	private void addGroupPages() {
		add(new TurboDevPageMapper("groups", GroupListPage.class));
		add(new TurboDevPageMapper("groups/new", NewGroupPage.class));
		add(new TurboDevPageMapper("groups/${group}/profile", GroupProfilePage.class));
		add(new TurboDevPageMapper("groups/${group}/members", GroupMembershipsPage.class));
		add(new TurboDevPageMapper("groups/${group}/authorizations", GroupAuthorizationsPage.class));
	}
	
	private void addProjectPages() {
		add(new TurboDevPageMapper("projects", ProjectListPage.class));
		add(new TurboDevPageMapper("projects/new", NewProjectPage.class));
		add(new TurboDevPageMapper("projects/${project}", ProjectBlobPage.class));

		add(new TurboDevPageMapper("projects/${project}/blob/#{revision}/#{path}", ProjectBlobPage.class) {
			
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
		add(new TurboDevPageMapper("projects/${project}/commit/${revision}", CommitDetailPage.class));
		add(new TurboDevPageMapper("projects/${project}/commits", ProjectCommitsPage.class));
		add(new TurboDevPageMapper("projects/${project}/compare", RevisionComparePage.class));
		add(new TurboDevPageMapper("projects/${project}/stats/contribs", ProjectContribsPage.class));
		add(new TurboDevPageMapper("projects/${project}/stats/lines", SourceLinesPage.class));

		add(new TurboDevPageMapper("projects/${project}/branches", ProjectBranchesPage.class));
		add(new TurboDevPageMapper("projects/${project}/tags", ProjectTagsPage.class));
		add(new TurboDevPageMapper("projects/${project}/codecomments", ProjectCodeCommentsPage.class));

		add(new TurboDevPageMapper("projects/${project}/pull", RequestListPage.class));
		add(new TurboDevPageMapper("projects/${project}/pull/new", NewRequestPage.class));
		add(new TurboDevPageMapper("projects/${project}/pull/${request}", RequestOverviewPage.class));
		add(new TurboDevPageMapper("projects/${project}/pull/${request}/overview", RequestOverviewPage.class));
		add(new TurboDevPageMapper("projects/${project}/pull/${request}/code-comments", RequestCodeCommentsPage.class));
		add(new TurboDevPageMapper("projects/${project}/pull/${request}/changes", RequestChangesPage.class));
		add(new TurboDevPageMapper("projects/${project}/pull/${request}/merge-preview", MergePreviewPage.class));
		add(new TurboDevPageMapper("projects/${project}/pull/${request}/invalid", InvalidRequestPage.class));

		add(new TurboDevPageMapper("projects/${project}/settings/general", GeneralSettingPage.class));
		add(new TurboDevPageMapper("projects/${project}/settings/authorizations", ProjectAuthorizationsPage.class));
		add(new TurboDevPageMapper("projects/${project}/settings/branch-protection", BranchProtectionPage.class));
		add(new TurboDevPageMapper("projects/${project}/settings/tag-protection", TagProtectionPage.class));
		add(new TurboDevPageMapper("projects/${project}/settings/commit-message-transform", CommitMessageTransformPage.class));
		
		add(new TurboDevPageMapper("projects/${project}/no-commits", NoCommitsPage.class));
	}

}
