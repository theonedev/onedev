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
import io.onedev.server.web.page.project.commit.CommitDetailPage;
import io.onedev.server.web.page.project.commit.ProjectCommitsPage;
import io.onedev.server.web.page.project.compare.RevisionComparePage;
import io.onedev.server.web.page.project.pullrequest.InvalidRequestPage;
import io.onedev.server.web.page.project.pullrequest.newrequest.NewRequestPage;
import io.onedev.server.web.page.project.pullrequest.requestdetail.changes.RequestChangesPage;
import io.onedev.server.web.page.project.pullrequest.requestdetail.codecomments.RequestCodeCommentsPage;
import io.onedev.server.web.page.project.pullrequest.requestdetail.mergepreview.MergePreviewPage;
import io.onedev.server.web.page.project.pullrequest.requestdetail.overview.RequestOverviewPage;
import io.onedev.server.web.page.project.pullrequest.requestlist.RequestListPage;
import io.onedev.server.web.page.project.setting.authorization.ProjectAuthorizationsPage;
import io.onedev.server.web.page.project.setting.branchprotection.BranchProtectionPage;
import io.onedev.server.web.page.project.setting.commitmessagetransform.CommitMessageTransformPage;
import io.onedev.server.web.page.project.setting.general.GeneralSettingPage;
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
import io.onedev.server.web.util.mapper.OneDevPageMapper;
import io.onedev.server.web.util.mapper.OneDevResourceMapper;
import io.onedev.server.web.util.resource.ArchiveResourceReference;
import io.onedev.server.web.util.resource.AttachmentResourceReference;
import io.onedev.server.web.util.resource.RawBlobResourceReference;
import io.onedev.server.web.util.resource.ServerLogResourceReference;

public class OneDevUrlMapper extends CompoundRequestMapper {

	public OneDevUrlMapper(WebApplication app) {
		add(new OneDevPageMapper("init", ServerInitPage.class));
		addAdministrationPages();
		addUserPages();
		addGroupPages();
		addProjectPages();
		addSecurityPages();
		
		add(new OneDevPageMapper("test", TestPage.class));
		
		addResources();
	}

	private void addResources() {
		add(new ResourceMapper("resource/server-log", new ServerLogResourceReference()));
		add(new ResourceMapper("projects/${project}/archive/${revision}", new ArchiveResourceReference()));
		add(new OneDevResourceMapper("projects/${project}/raw/${revision}/${path}", new RawBlobResourceReference()));
		add(new ResourceMapper("projects/${project}/attachment/${uuid}/${attachment}", 
				new AttachmentResourceReference()));
	}
	
	private void addSecurityPages() {
		add(new OneDevPageMapper("login", LoginPage.class));
		add(new OneDevPageMapper("logout", LogoutPage.class));
		add(new OneDevPageMapper("register", RegisterPage.class));
		add(new OneDevPageMapper("forget", ForgetPage.class));
	}
	
	private void addAdministrationPages() {
		add(new OneDevPageMapper("administration/settings/system", SystemSettingPage.class));
		add(new OneDevPageMapper("administration/settings/mail", MailSettingPage.class));
		add(new OneDevPageMapper("administration/settings/backup", DatabaseBackupPage.class));
		add(new OneDevPageMapper("administration/settings/security", SecuritySettingPage.class));
		add(new OneDevPageMapper("administration/settings/authenticator", AuthenticatorPage.class));
		add(new OneDevPageMapper("administration/server-log", ServerLogPage.class));
		add(new OneDevPageMapper("administration/server-information", ServerInformationPage.class));
		add(new OneDevPageMapper("administration/license-management", LicenseManagementPage.class));
	}
	
	private void addUserPages() {
		add(new OneDevPageMapper("users", UserListPage.class));
		add(new OneDevPageMapper("users/new", NewUserPage.class));
		add(new OneDevPageMapper("users/${user}/profile", UserProfilePage.class));
		add(new OneDevPageMapper("users/${user}/groups", UserMembershipsPage.class));
		add(new OneDevPageMapper("users/${user}/authorizations", UserAuthorizationsPage.class));
		add(new OneDevPageMapper("users/${user}/tasks", TaskListPage.class));
		add(new OneDevPageMapper("users/${user}/avatar", AvatarEditPage.class));
		add(new OneDevPageMapper("users/${user}/password", PasswordEditPage.class));
		add(new OneDevPageMapper("users/${user}/token", TokenGeneratePage.class));
	}

	private void addGroupPages() {
		add(new OneDevPageMapper("groups", GroupListPage.class));
		add(new OneDevPageMapper("groups/new", NewGroupPage.class));
		add(new OneDevPageMapper("groups/${group}/profile", GroupProfilePage.class));
		add(new OneDevPageMapper("groups/${group}/members", GroupMembershipsPage.class));
		add(new OneDevPageMapper("groups/${group}/authorizations", GroupAuthorizationsPage.class));
	}
	
	private void addProjectPages() {
		add(new OneDevPageMapper("projects", ProjectListPage.class) {
			
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
		add(new OneDevPageMapper("projects/new", NewProjectPage.class));
		add(new OneDevPageMapper("projects/${project}", ProjectBlobPage.class));

		add(new OneDevPageMapper("projects/${project}/blob/#{revision}/#{path}", ProjectBlobPage.class) {
			
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
		add(new OneDevPageMapper("projects/${project}/commit/${revision}", CommitDetailPage.class));
		add(new OneDevPageMapper("projects/${project}/commits", ProjectCommitsPage.class));
		add(new OneDevPageMapper("projects/${project}/compare", RevisionComparePage.class));
		add(new OneDevPageMapper("projects/${project}/stats/contribs", ProjectContribsPage.class));
		add(new OneDevPageMapper("projects/${project}/stats/lines", SourceLinesPage.class));

		add(new OneDevPageMapper("projects/${project}/branches", ProjectBranchesPage.class));
		add(new OneDevPageMapper("projects/${project}/tags", ProjectTagsPage.class));
		add(new OneDevPageMapper("projects/${project}/codecomments", ProjectCodeCommentsPage.class));

		add(new OneDevPageMapper("projects/${project}/pull", RequestListPage.class));
		add(new OneDevPageMapper("projects/${project}/pull/new", NewRequestPage.class));
		add(new OneDevPageMapper("projects/${project}/pull/${request}", RequestOverviewPage.class));
		add(new OneDevPageMapper("projects/${project}/pull/${request}/overview", RequestOverviewPage.class));
		add(new OneDevPageMapper("projects/${project}/pull/${request}/code-comments", RequestCodeCommentsPage.class));
		add(new OneDevPageMapper("projects/${project}/pull/${request}/changes", RequestChangesPage.class));
		add(new OneDevPageMapper("projects/${project}/pull/${request}/merge-preview", MergePreviewPage.class));
		add(new OneDevPageMapper("projects/${project}/pull/${request}/invalid", InvalidRequestPage.class));

		add(new OneDevPageMapper("projects/${project}/settings/general", GeneralSettingPage.class));
		add(new OneDevPageMapper("projects/${project}/settings/authorizations", ProjectAuthorizationsPage.class));
		add(new OneDevPageMapper("projects/${project}/settings/branch-protection", BranchProtectionPage.class));
		add(new OneDevPageMapper("projects/${project}/settings/tag-protection", TagProtectionPage.class));
		add(new OneDevPageMapper("projects/${project}/settings/commit-message-transform", CommitMessageTransformPage.class));
		
		add(new OneDevPageMapper("projects/${project}/no-commits", NoCommitsPage.class));
	}

}
