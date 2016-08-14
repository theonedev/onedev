package com.pmease.gitplex.web.mapper;

import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.mapper.CompoundRequestMapper;

import com.pmease.commons.wicket.NoVersionMountedMapper;
import com.pmease.gitplex.web.page.account.collaborators.AccountCollaboratorListPage;
import com.pmease.gitplex.web.page.account.collaborators.CollaboratorDepotListPage;
import com.pmease.gitplex.web.page.account.collaborators.CollaboratorEffectivePrivilegePage;
import com.pmease.gitplex.web.page.account.collaborators.CollaboratorPrivilegeSourcePage;
import com.pmease.gitplex.web.page.account.members.MemberEffectivePrivilegePage;
import com.pmease.gitplex.web.page.account.members.MemberListPage;
import com.pmease.gitplex.web.page.account.members.MemberPrivilegeSourcePage;
import com.pmease.gitplex.web.page.account.members.MemberTeamListPage;
import com.pmease.gitplex.web.page.account.members.NewMembersPage;
import com.pmease.gitplex.web.page.account.notifications.NotificationListPage;
import com.pmease.gitplex.web.page.account.overview.AccountOverviewPage;
import com.pmease.gitplex.web.page.account.overview.NewDepotPage;
import com.pmease.gitplex.web.page.account.overview.NewOrganizationPage;
import com.pmease.gitplex.web.page.account.setting.AvatarEditPage;
import com.pmease.gitplex.web.page.account.setting.PasswordEditPage;
import com.pmease.gitplex.web.page.account.setting.ProfileEditPage;
import com.pmease.gitplex.web.page.account.teams.NewTeamPage;
import com.pmease.gitplex.web.page.account.teams.TeamDepotListPage;
import com.pmease.gitplex.web.page.account.teams.TeamEditPage;
import com.pmease.gitplex.web.page.account.teams.TeamListPage;
import com.pmease.gitplex.web.page.account.teams.TeamMemberListPage;
import com.pmease.gitplex.web.page.admin.MailSettingPage;
import com.pmease.gitplex.web.page.admin.SystemSettingPage;
import com.pmease.gitplex.web.page.admin.account.AccountListPage;
import com.pmease.gitplex.web.page.admin.account.NewUserPage;
import com.pmease.gitplex.web.page.depot.NoBranchesPage;
import com.pmease.gitplex.web.page.depot.branches.DepotBranchesPage;
import com.pmease.gitplex.web.page.depot.comments.CodeCommentPage;
import com.pmease.gitplex.web.page.depot.comments.DepotCommentsPage;
import com.pmease.gitplex.web.page.depot.commit.CommitDetailPage;
import com.pmease.gitplex.web.page.depot.commit.DepotCommitsPage;
import com.pmease.gitplex.web.page.depot.compare.RevisionComparePage;
import com.pmease.gitplex.web.page.depot.file.DepotFilePage;
import com.pmease.gitplex.web.page.depot.overview.DepotOverviewPage;
import com.pmease.gitplex.web.page.depot.pullrequest.newrequest.NewRequestPage;
import com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.changes.RequestChangesPage;
import com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.codecomments.RequestCodeCommentsPage;
import com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.integrationpreview.IntegrationPreviewPage;
import com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.overview.RequestOverviewPage;
import com.pmease.gitplex.web.page.depot.pullrequest.requestlist.RequestListPage;
import com.pmease.gitplex.web.page.depot.setting.authorization.DepotCollaboratorListPage;
import com.pmease.gitplex.web.page.depot.setting.authorization.DepotEffectivePrivilegePage;
import com.pmease.gitplex.web.page.depot.setting.authorization.DepotTeamListPage;
import com.pmease.gitplex.web.page.depot.setting.gatekeeper.GateKeeperPage;
import com.pmease.gitplex.web.page.depot.setting.general.GeneralSettingPage;
import com.pmease.gitplex.web.page.depot.setting.integrationpolicy.IntegrationPolicyPage;
import com.pmease.gitplex.web.page.depot.tags.DepotTagsPage;
import com.pmease.gitplex.web.page.init.ServerInitPage;
import com.pmease.gitplex.web.page.init.WelcomePage;
import com.pmease.gitplex.web.page.layout.CreateDepotPage;
import com.pmease.gitplex.web.page.security.ForgetPage;
import com.pmease.gitplex.web.page.security.LoginPage;
import com.pmease.gitplex.web.page.security.LogoutPage;
import com.pmease.gitplex.web.page.security.RegisterPage;
import com.pmease.gitplex.web.page.test.RunModePage;
import com.pmease.gitplex.web.page.test.TestPage;
import com.pmease.gitplex.web.resource.ArchiveResourceReference;
import com.pmease.gitplex.web.resource.AttachmentResourceReference;
import com.pmease.gitplex.web.resource.BlobResourceReference;

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
		add(new NoVersionMountedMapper("new-depot", CreateDepotPage.class));
		
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
		add(new NoVersionMountedMapper("administration/accounts", AccountListPage.class));
		add(new NoVersionMountedMapper("administration/accounts/new", NewUserPage.class));
		add(new NoVersionMountedMapper("administration/settings/system", SystemSettingPage.class));
		add(new NoVersionMountedMapper("administration/settings/mail", MailSettingPage.class));
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
		add(new NoVersionMountedMapper("accounts/${account}/notifications", NotificationListPage.class));
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
		add(new NoVersionMountedMapper("${account}/${depot}", DepotOverviewPage.class) {

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
		
		add(new DepotMapper("${account}/${depot}/no-branches", NoBranchesPage.class));
	}

}
