package com.gitplex.server.web;

import org.apache.wicket.core.request.handler.BookmarkablePageRequestHandler;
import org.apache.wicket.core.request.handler.IPageClassRequestHandler;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler;
import org.apache.wicket.core.request.mapper.ResourceMapper;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.mapper.CompoundRequestMapper;

import com.gitplex.server.web.page.admin.DatabaseBackupPage;
import com.gitplex.server.web.page.admin.MailSettingPage;
import com.gitplex.server.web.page.admin.SecuritySettingPage;
import com.gitplex.server.web.page.admin.ServerInformationPage;
import com.gitplex.server.web.page.admin.ServerLogPage;
import com.gitplex.server.web.page.admin.SystemSettingPage;
import com.gitplex.server.web.page.group.GroupAuthorizationsPage;
import com.gitplex.server.web.page.group.GroupListPage;
import com.gitplex.server.web.page.group.GroupMembershipsPage;
import com.gitplex.server.web.page.group.GroupProfilePage;
import com.gitplex.server.web.page.group.NewGroupPage;
import com.gitplex.server.web.page.init.ServerInitPage;
import com.gitplex.server.web.page.project.NewProjectPage;
import com.gitplex.server.web.page.project.NoBranchesPage;
import com.gitplex.server.web.page.project.ProjectListPage;
import com.gitplex.server.web.page.project.blob.ProjectBlobPage;
import com.gitplex.server.web.page.project.branches.ProjectBranchesPage;
import com.gitplex.server.web.page.project.commit.CommitDetailPage;
import com.gitplex.server.web.page.project.commit.ProjectCommitsPage;
import com.gitplex.server.web.page.project.compare.RevisionComparePage;
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
import com.gitplex.server.web.page.user.UserAuthorizationsPage;
import com.gitplex.server.web.page.user.UserListPage;
import com.gitplex.server.web.page.user.UserMembershipsPage;
import com.gitplex.server.web.page.user.UserProfilePage;
import com.gitplex.server.web.util.mapper.WebPageMapper;
import com.gitplex.server.web.util.resource.ArchiveResourceReference;
import com.gitplex.server.web.util.resource.AttachmentResourceReference;
import com.gitplex.server.web.util.resource.RawBlobResourceReference;
import com.gitplex.server.web.util.resource.ServerLogResourceReference;

public class GitPlexUrlMapper extends CompoundRequestMapper {

	public GitPlexUrlMapper(WebApplication app) {
		add(new WebPageMapper("init", ServerInitPage.class));
		addAdministrationPages();
		addUserPages();
		addGroupPages();
		addProjectPages();
		addSecurityPages();
		
		add(new WebPageMapper("test", TestPage.class));
		
		addResources();
	}

	private void addResources() {
		add(new ResourceMapper("resource/server-log", new ServerLogResourceReference()));
		add(new ResourceMapper("projects/${project}/archive/${revision}", new ArchiveResourceReference()));
		add(new ResourceMapper("projects/${project}/raw/${revision}/${path}", new RawBlobResourceReference()));
		add(new ResourceMapper("projects/${project}/attachment/${uuid}/${attachment}", 
				new AttachmentResourceReference()));
	}
	
	private void addSecurityPages() {
		add(new WebPageMapper("login", LoginPage.class));
		add(new WebPageMapper("logout", LogoutPage.class));
		add(new WebPageMapper("register", RegisterPage.class));
		add(new WebPageMapper("forget", ForgetPage.class));
	}
	
	private void addAdministrationPages() {
		add(new WebPageMapper("administration/settings/system", SystemSettingPage.class));
		add(new WebPageMapper("administration/settings/mail", MailSettingPage.class));
		add(new WebPageMapper("administration/settings/backup", DatabaseBackupPage.class));
		add(new WebPageMapper("administration/settings/security", SecuritySettingPage.class));
		add(new WebPageMapper("administration/server-log", ServerLogPage.class));
		add(new WebPageMapper("administration/server-information", ServerInformationPage.class));
	}
	
	private void addUserPages() {
		add(new WebPageMapper("users", UserListPage.class));
		add(new WebPageMapper("users/new", NewUserPage.class));
		add(new WebPageMapper("users/${user}/profile", UserProfilePage.class));
		add(new WebPageMapper("users/${user}/groups", UserMembershipsPage.class));
		add(new WebPageMapper("users/${user}/authorizations", UserAuthorizationsPage.class));
		add(new WebPageMapper("users/${user}/tasks", TaskListPage.class));
		add(new WebPageMapper("users/${user}/avatar", AvatarEditPage.class));
		add(new WebPageMapper("users/${user}/password", PasswordEditPage.class));
	}

	private void addGroupPages() {
		add(new WebPageMapper("groups", GroupListPage.class));
		add(new WebPageMapper("groups/new", NewGroupPage.class));
		add(new WebPageMapper("groups/${group}/profile", GroupProfilePage.class));
		add(new WebPageMapper("groups/${group}/members", GroupMembershipsPage.class));
		add(new WebPageMapper("groups/${group}/authorizations", GroupAuthorizationsPage.class));
	}
	
	private void addProjectPages() {
		add(new WebPageMapper("projects", ProjectListPage.class));
		add(new WebPageMapper("projects/new", NewProjectPage.class));
		add(new WebPageMapper("projects/${project}", ProjectBlobPage.class));

		add(new WebPageMapper("projects/${project}/blob/#{revision}/#{path}", ProjectBlobPage.class) {
			
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
		add(new WebPageMapper("projects/${project}/commit/${revision}", CommitDetailPage.class));
		add(new WebPageMapper("projects/${project}/commits", ProjectCommitsPage.class));
		add(new WebPageMapper("projects/${project}/compare", RevisionComparePage.class));

		add(new WebPageMapper("projects/${project}/branches", ProjectBranchesPage.class));
		add(new WebPageMapper("projects/${project}/tags", ProjectTagsPage.class));

		add(new WebPageMapper("projects/${project}/pulls", RequestListPage.class));
		add(new WebPageMapper("projects/${project}/pulls/new", NewRequestPage.class));
		add(new WebPageMapper("projects/${project}/pulls/${request}", RequestOverviewPage.class));
		add(new WebPageMapper("projects/${project}/pulls/${request}/overview", RequestOverviewPage.class));
		add(new WebPageMapper("projects/${project}/pulls/${request}/code-comments", RequestCodeCommentsPage.class));
		add(new WebPageMapper("projects/${project}/pulls/${request}/changes", RequestChangesPage.class));
		add(new WebPageMapper("projects/${project}/pulls/${request}/merge-preview", MergePreviewPage.class));

		add(new WebPageMapper("projects/${project}/settings/general", GeneralSettingPage.class));
		add(new WebPageMapper("projects/${project}/settings/authorizations", ProjectAuthorizationsPage.class));
		add(new WebPageMapper("projects/${project}/settings/branch-protection", BranchProtectionPage.class));
		add(new WebPageMapper("projects/${project}/settings/tag-protection", TagProtectionPage.class));
		add(new WebPageMapper("projects/${project}/settings/commit-message-transform", CommitMessageTransformPage.class));
		
		add(new WebPageMapper("projects/${project}/no-branches", NoBranchesPage.class));
	}

}
