package com.gitplex.server.web.mapper;

import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.mapper.CompoundRequestMapper;

import com.gitplex.commons.wicket.NoVersionMountedMapper;
import com.gitplex.server.web.page.account.collaborators.AccountCollaboratorListPage;
import com.gitplex.server.web.page.account.collaborators.CollaboratorDepotListPage;
import com.gitplex.server.web.page.account.collaborators.CollaboratorEffectivePrivilegePage;
import com.gitplex.server.web.page.account.collaborators.CollaboratorPrivilegeSourcePage;
import com.gitplex.server.web.page.account.members.MemberEffectivePrivilegePage;
import com.gitplex.server.web.page.account.members.MemberListPage;
import com.gitplex.server.web.page.account.members.MemberPrivilegeSourcePage;
import com.gitplex.server.web.page.account.members.MemberTeamListPage;
import com.gitplex.server.web.page.account.members.NewMembersPage;
import com.gitplex.server.web.page.account.overview.AccountOverviewPage;
import com.gitplex.server.web.page.account.overview.NewDepotPage;
import com.gitplex.server.web.page.account.overview.NewOrganizationPage;
import com.gitplex.server.web.page.account.setting.AvatarEditPage;
import com.gitplex.server.web.page.account.setting.PasswordEditPage;
import com.gitplex.server.web.page.account.setting.ProfileEditPage;
import com.gitplex.server.web.page.account.tasks.TaskListPage;
import com.gitplex.server.web.page.account.teams.NewTeamPage;
import com.gitplex.server.web.page.account.teams.TeamDepotListPage;
import com.gitplex.server.web.page.account.teams.TeamEditPage;
import com.gitplex.server.web.page.account.teams.TeamListPage;
import com.gitplex.server.web.page.account.teams.TeamMemberListPage;
import com.gitplex.server.web.page.admin.DatabaseBackupPage;
import com.gitplex.server.web.page.admin.MailSettingPage;
import com.gitplex.server.web.page.admin.SecuritySettingPage;
import com.gitplex.server.web.page.admin.SystemSettingPage;
import com.gitplex.server.web.page.admin.account.NewUserPage;
import com.gitplex.server.web.page.admin.account.UserListPage;
import com.gitplex.server.web.page.depot.NoBranchesPage;
import com.gitplex.server.web.page.depot.branches.DepotBranchesPage;
import com.gitplex.server.web.page.depot.comments.CodeCommentPage;
import com.gitplex.server.web.page.depot.comments.DepotCommentsPage;
import com.gitplex.server.web.page.depot.commit.CommitDetailPage;
import com.gitplex.server.web.page.depot.commit.DepotCommitsPage;
import com.gitplex.server.web.page.depot.compare.RevisionComparePage;
import com.gitplex.server.web.page.depot.file.DepotFilePage;
import com.gitplex.server.web.page.depot.pullrequest.newrequest.NewRequestPage;
import com.gitplex.server.web.page.depot.pullrequest.requestdetail.changes.RequestChangesPage;
import com.gitplex.server.web.page.depot.pullrequest.requestdetail.codecomments.RequestCodeCommentsPage;
import com.gitplex.server.web.page.depot.pullrequest.requestdetail.integrationpreview.IntegrationPreviewPage;
import com.gitplex.server.web.page.depot.pullrequest.requestdetail.overview.RequestOverviewPage;
import com.gitplex.server.web.page.depot.pullrequest.requestlist.RequestListPage;
import com.gitplex.server.web.page.depot.setting.authorization.DepotCollaboratorListPage;
import com.gitplex.server.web.page.depot.setting.authorization.DepotEffectivePrivilegePage;
import com.gitplex.server.web.page.depot.setting.authorization.DepotTeamListPage;
import com.gitplex.server.web.page.depot.setting.commitmessagetransform.CommitMessageTransformPage;
import com.gitplex.server.web.page.depot.setting.gatekeeper.GateKeeperPage;
import com.gitplex.server.web.page.depot.setting.general.GeneralSettingPage;
import com.gitplex.server.web.page.depot.setting.integrationpolicy.IntegrationPolicyPage;
import com.gitplex.server.web.page.depot.tags.DepotTagsPage;
import com.gitplex.server.web.page.init.ServerInitPage;
import com.gitplex.server.web.page.init.WelcomePage;
import com.gitplex.server.web.page.layout.CreateDepotPage;
import com.gitplex.server.web.page.security.ForgetPage;
import com.gitplex.server.web.page.security.LoginPage;
import com.gitplex.server.web.page.security.LogoutPage;
import com.gitplex.server.web.page.security.RegisterPage;
import com.gitplex.server.web.page.test.RunModePage;
import com.gitplex.server.web.page.test.TestPage;
import com.gitplex.server.web.resource.ArchiveResourceReference;
import com.gitplex.server.web.resource.AttachmentResourceReference;
import com.gitplex.server.web.resource.BlobResourceReference;

public class UrlMapper extends CompoundRequestMapper {

	public UrlMapper(WebApplication app) {
		add(new NoVersionMountedMapper("init", ServerInitPage.class));
		add(new NoVersionMountedMapper("welcome", WelcomePage.class));
		addAdministrationPages();
		addAccountPages();
		addDepotPages();
		addSecurityPages();
		
		add(new NoVersionMountedMapper("test", TestPage.class));
		add(new NoVersionMountedMapper("runmode", RunModePage.class));
		add(new NoVersionMountedMapper("new-repository", CreateDepotPage.class));
		
		addResources();
	}

	private void addResources() {
		add(new DepotResourceMapper("${account}/${depot}/archive", new ArchiveResourceReference()));
		add(new DepotResourceMapper("${account}/${depot}/raw", new BlobResourceReference()));
		add(new DepotResourceMapper("${account}/${depot}/attachment/${uuid}/${attachment}", 
				new AttachmentResourceReference()));
	}
	
	private void addSecurityPages() {
		add(new NoVersionMountedMapper("login", LoginPage.class));
		add(new NoVersionMountedMapper("logout", LogoutPage.class));
		add(new NoVersionMountedMapper("register", RegisterPage.class));
		add(new NoVersionMountedMapper("forget", ForgetPage.class));
	}
	
	private void addAdministrationPages() {
		add(new NoVersionMountedMapper("administration/users", UserListPage.class));
		add(new NoVersionMountedMapper("administration/users/new", NewUserPage.class));
		add(new NoVersionMountedMapper("administration/settings/system", SystemSettingPage.class));
		add(new NoVersionMountedMapper("administration/settings/mail", MailSettingPage.class));
		add(new NoVersionMountedMapper("administration/settings/backup", DatabaseBackupPage.class));
		add(new NoVersionMountedMapper("administration/settings/security", SecuritySettingPage.class));
	}
	
	private void addAccountPages() {
		add(new NoVersionMountedMapper("${account}", AccountOverviewPage.class) {

			@Override
			public IRequestHandler mapRequest(Request request) {
				if (MapperUtils.getAccountSegments(request.getUrl()) == 1)
					return super.mapRequest(request);
				else
					return null;
			}
			
		});
		
		add(new NoVersionMountedMapper("accounts/${account}/new-depot", NewDepotPage.class));
		add(new NoVersionMountedMapper("accounts/${account}/new-organization", NewOrganizationPage.class));
		add(new NoVersionMountedMapper("accounts/${account}/tasks", TaskListPage.class));
		add(new NoVersionMountedMapper("accounts/${account}/settings/profile", ProfileEditPage.class));
		add(new NoVersionMountedMapper("accounts/${account}/settings/avatar", AvatarEditPage.class));
		add(new NoVersionMountedMapper("accounts/${account}/settings/password", PasswordEditPage.class));

		add(new NoVersionMountedMapper("accounts/${account}/members", MemberListPage.class));
		add(new NoVersionMountedMapper("accounts/${account}/members/${member}/teams", MemberTeamListPage.class));
		add(new NoVersionMountedMapper("accounts/${account}/members/${member}/depots", MemberEffectivePrivilegePage.class));
		add(new NoVersionMountedMapper("accounts/${account}/members/${member}/depots/${depot}", MemberPrivilegeSourcePage.class));
		add(new NoVersionMountedMapper("accounts/${account}/members/new", NewMembersPage.class));
		add(new NoVersionMountedMapper("accounts/${account}/teams", TeamListPage.class));
		add(new NoVersionMountedMapper("accounts/${account}/teams/new", NewTeamPage.class));
		add(new NoVersionMountedMapper("accounts/${account}/teams/${team}/setting", TeamEditPage.class));
		add(new NoVersionMountedMapper("accounts/${account}/teams/${team}/members", TeamMemberListPage.class));
		add(new NoVersionMountedMapper("accounts/${account}/teams/${team}/depots", TeamDepotListPage.class));

		add(new NoVersionMountedMapper("accounts/${account}/collaborators", AccountCollaboratorListPage.class));
		add(new NoVersionMountedMapper(
				"accounts/${account}/collaborators/${collaborator}/depots", 
				CollaboratorDepotListPage.class));
		add(new NoVersionMountedMapper(
				"accounts/${account}/collaborators/${collaborator}/effective", 
				CollaboratorEffectivePrivilegePage.class));
		add(new NoVersionMountedMapper(
				"accounts/${account}/collaborators/${collaborator}/effective/${depot}", 
				CollaboratorPrivilegeSourcePage.class));
	}

	private void addDepotPages() {
		add(new NoVersionMountedMapper("${account}/${depot}", DepotFilePage.class) {

			@Override
			public IRequestHandler mapRequest(Request request) {
				if (MapperUtils.getDepotSegments(request.getUrl()) == 2)
					return super.mapRequest(request);
				else
					return null;
			}
			
		});

		add(new DepotMapper("${account}/${depot}/files", DepotFilePage.class));
		add(new DepotMapper("${account}/${depot}/commit", CommitDetailPage.class));
		add(new DepotMapper("${account}/${depot}/commits", DepotCommitsPage.class));
		add(new DepotMapper("${account}/${depot}/compare", RevisionComparePage.class));

		add(new DepotMapper("${account}/${depot}/branches", DepotBranchesPage.class));
		add(new DepotMapper("${account}/${depot}/tags", DepotTagsPage.class));

		add(new DepotMapper("${account}/${depot}/pulls", RequestListPage.class));
		add(new DepotMapper("${account}/${depot}/pulls/new", NewRequestPage.class));
		add(new DepotMapper("${account}/${depot}/pulls/${request}", RequestOverviewPage.class));
		add(new DepotMapper(
				"${account}/${depot}/pulls/${request}/overview", RequestOverviewPage.class));
		add(new DepotMapper(
				"${account}/${depot}/pulls/${request}/code-comments", RequestCodeCommentsPage.class));
		add(new DepotMapper(
				"${account}/${depot}/pulls/${request}/changes", RequestChangesPage.class));
		add(new DepotMapper(
				"${account}/${depot}/pulls/${request}/integration-preview", IntegrationPreviewPage.class));
		add(new DepotMapper("${account}/${depot}/comments", DepotCommentsPage.class));
		add(new DepotMapper("${account}/${depot}/comments/${comment}", CodeCommentPage.class));

		add(new DepotMapper("${account}/${depot}/settings/general", GeneralSettingPage.class));
		add(new DepotMapper("${account}/${depot}/settings/teams", DepotTeamListPage.class));
		add(new DepotMapper("${account}/${depot}/settings/collaborators", DepotCollaboratorListPage.class));
		add(new DepotMapper("${account}/${depot}/settings/effective-privilege", DepotEffectivePrivilegePage.class));
		add(new DepotMapper("${account}/${depot}/settings/gate-keeper", GateKeeperPage.class));
		add(new DepotMapper("${account}/${depot}/settings/integration-policy", IntegrationPolicyPage.class));
		add(new DepotMapper("${account}/${depot}/settings/commit-message-transform", CommitMessageTransformPage.class));
		
		add(new DepotMapper("${account}/${depot}/no-branches", NoBranchesPage.class));
	}

}
