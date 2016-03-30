package com.pmease.gitplex.web;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.core.request.mapper.MountedMapper;
import org.apache.wicket.core.request.mapper.ResourceMapper;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.mapper.CompoundRequestMapper;

import com.pmease.commons.util.StringUtils;
import com.pmease.gitplex.core.util.validation.AccountNameValidator;
import com.pmease.gitplex.core.util.validation.DepotNameValidator;
import com.pmease.gitplex.web.page.account.collaborators.AccountCollaboratorListPage;
import com.pmease.gitplex.web.page.account.collaborators.CollaboratorDepotListPage;
import com.pmease.gitplex.web.page.account.collaborators.CollaboratorEffectivePrivilegePage;
import com.pmease.gitplex.web.page.account.collaborators.CollaboratorPrivilegeSourcePage;
import com.pmease.gitplex.web.page.account.depots.DepotListPage;
import com.pmease.gitplex.web.page.account.depots.NewDepotPage;
import com.pmease.gitplex.web.page.account.members.MemberEffectivePrivilegePage;
import com.pmease.gitplex.web.page.account.members.MemberListPage;
import com.pmease.gitplex.web.page.account.members.MemberPrivilegeSourcePage;
import com.pmease.gitplex.web.page.account.members.MemberTeamListPage;
import com.pmease.gitplex.web.page.account.members.NewMembersPage;
import com.pmease.gitplex.web.page.account.notifications.NotificationListPage;
import com.pmease.gitplex.web.page.account.organizations.NewOrganizationPage;
import com.pmease.gitplex.web.page.account.organizations.OrganizationListPage;
import com.pmease.gitplex.web.page.account.overview.AccountOverviewPage;
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
import com.pmease.gitplex.web.page.depot.NoCommitsPage;
import com.pmease.gitplex.web.page.depot.branches.DepotBranchesPage;
import com.pmease.gitplex.web.page.depot.commit.CommitDetailPage;
import com.pmease.gitplex.web.page.depot.commit.DepotCommitsPage;
import com.pmease.gitplex.web.page.depot.compare.RevisionComparePage;
import com.pmease.gitplex.web.page.depot.file.DepotFilePage;
import com.pmease.gitplex.web.page.depot.pullrequest.newrequest.NewRequestPage;
import com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.attachments.RequestAttachmentsPage;
import com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.compare.RequestComparePage;
import com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.overview.RequestOverviewPage;
import com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.updates.RequestUpdatesPage;
import com.pmease.gitplex.web.page.depot.pullrequest.requestlist.RequestListPage;
import com.pmease.gitplex.web.page.depot.setting.authorization.DepotEffectivePrivilegePage;
import com.pmease.gitplex.web.page.depot.setting.authorization.DepotTeamListPage;
import com.pmease.gitplex.web.page.depot.setting.authorization.DepotCollaboratorListPage;
import com.pmease.gitplex.web.page.depot.setting.gatekeeper.GateKeeperPage;
import com.pmease.gitplex.web.page.depot.setting.general.GeneralSettingPage;
import com.pmease.gitplex.web.page.depot.setting.integrationpolicy.IntegrationPolicyPage;
import com.pmease.gitplex.web.page.depot.tags.DepotTagsPage;
import com.pmease.gitplex.web.page.init.ServerInitPage;
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
		add(new MountedMapper("init", ServerInitPage.class));
		addAdministrationPages();
		addAccountPages();
		addDepotPages();
		addSecurityPages();
		
		add(new MountedMapper("test", TestPage.class));
		add(new MountedMapper("runmode", RunModePage.class));
		
		addResources();
	}

	private void addResources() {
		add(new ResourceMapper("${account}/${depot}/archive", new ArchiveResourceReference()) {

			@Override
			public int getCompatibilityScore(Request request) {
				return 3;
			}
			
		});
		add(new ResourceMapper("${account}/${depot}/raw", new BlobResourceReference()) {

			@Override
			public int getCompatibilityScore(Request request) {
				return 3;
			}
			
		});
		add(new ResourceMapper("${account}/${depot}/pulls/${request}/attachments/${attachment}", new AttachmentResourceReference()) {

			@Override
			public int getCompatibilityScore(Request request) {
				return 8;
			}
			
		});
	}
	
	private void addSecurityPages() {
		add(new MountedMapper("login", LoginPage.class));
		add(new MountedMapper("logout", LogoutPage.class));
		add(new MountedMapper("register", RegisterPage.class));
		add(new MountedMapper("forget", ForgetPage.class));
	}
	
	private void addAdministrationPages() {
		add(new MountedMapper("administration/accounts", AccountListPage.class));
		add(new MountedMapper("administration/accounts/new", NewUserPage.class));
		add(new MountedMapper("administration/settings/system", SystemSettingPage.class));
		add(new MountedMapper("administration/settings/mail", MailSettingPage.class));
	}
	
	public List<String> normalize(List<String> urlSegments) {
		List<String> normalized = new ArrayList<String>();
		for (String each: urlSegments) {
			each = StringUtils.remove(each, '/');
			if (each.length() != 0)
				normalized.add(each);
		}
		return normalized;
	}

	private void addAccountPages() {
		add(new MountedMapper("${account}", AccountOverviewPage.class) {

			@Override
			protected boolean urlStartsWith(Url url, String... segments) {
				List<String> urlSegments = normalize(url.getSegments());
				if (urlSegments.size() < 1)
					return false;
				String accountName = urlSegments.get(0);
				
				return !AccountNameValidator.getReservedNames().contains(accountName);
			}

		});
		
		add(new MountedMapper("accounts/${account}/depots", DepotListPage.class));
		add(new MountedMapper("accounts/${account}/depots/new", NewDepotPage.class));
		add(new MountedMapper("accounts/${account}/organizations", OrganizationListPage.class));
		add(new MountedMapper("accounts/${account}/organizations/new", NewOrganizationPage.class));
		add(new MountedMapper("accounts/${account}/notifications", NotificationListPage.class));
		add(new MountedMapper("accounts/${account}/settings/profile", ProfileEditPage.class));
		add(new MountedMapper("accounts/${account}/settings/avatar", AvatarEditPage.class));
		add(new MountedMapper("accounts/${account}/settings/password", PasswordEditPage.class));

		add(new MountedMapper("accounts/${account}/members", MemberListPage.class));
		add(new MountedMapper("accounts/${account}/members/${member}/teams", MemberTeamListPage.class));
		add(new MountedMapper("accounts/${account}/members/${member}/depots", MemberEffectivePrivilegePage.class));
		add(new MountedMapper("accounts/${account}/members/${member}/depots/${depot}", MemberPrivilegeSourcePage.class));
		add(new MountedMapper("accounts/${account}/members/new", NewMembersPage.class));
		add(new MountedMapper("accounts/${account}/teams", TeamListPage.class));
		add(new MountedMapper("accounts/${account}/teams/new", NewTeamPage.class));
		add(new MountedMapper("accounts/${account}/teams/${team}/setting", TeamEditPage.class));
		add(new MountedMapper("accounts/${account}/teams/${team}/members", TeamMemberListPage.class));
		add(new MountedMapper("accounts/${account}/teams/${team}/depots", TeamDepotListPage.class));

		add(new MountedMapper("accounts/${account}/collaborators", AccountCollaboratorListPage.class));
		add(new MountedMapper(
				"accounts/${account}/collaborators/${collaborator}/depots", 
				CollaboratorDepotListPage.class));
		add(new MountedMapper(
				"accounts/${account}/collaborators/${collaborator}/effective", 
				CollaboratorEffectivePrivilegePage.class));
		add(new MountedMapper(
				"accounts/${account}/collaborators/${collaborator}/effective/${depot}", 
				CollaboratorPrivilegeSourcePage.class));
	}

	private void addDepotPages() {
		add(new MountedMapper("${account}/${depot}", DepotFilePage.class) {

			@Override
			protected boolean urlStartsWith(Url url, String... segments) {
				List<String> urlSegments = normalize(url.getSegments());
				if (urlSegments.size() < 2)
					return false;
				String accountName = urlSegments.get(0);
				if (AccountNameValidator.getReservedNames().contains(accountName))
					return false;

				String depotName = urlSegments.get(1);
				return !DepotNameValidator.getReservedNames().contains(depotName);
			}

		});

//		add(new ParameterAwareMountedMapper("${account}/${depot}/browse", RepoFilePage.class));
		add(new MountedMapper("${account}/${depot}/commit", CommitDetailPage.class));
		add(new MountedMapper("${account}/${depot}/commits", DepotCommitsPage.class));
		add(new MountedMapper("${account}/${depot}/compare", RevisionComparePage.class));

		add(new MountedMapper("${account}/${depot}/branches", DepotBranchesPage.class));
		add(new MountedMapper("${account}/${depot}/tags", DepotTagsPage.class));

		add(new MountedMapper("${account}/${depot}/pulls", RequestListPage.class));
		add(new MountedMapper("${account}/${depot}/pulls/new", NewRequestPage.class));
		add(new MountedMapper("${account}/${depot}/pulls/${request}", RequestOverviewPage.class));
		add(new MountedMapper(
				"${account}/${depot}/pulls/${request}/overview", RequestOverviewPage.class));
		add(new MountedMapper(
				"${account}/${depot}/pulls/${request}/updates", RequestUpdatesPage.class));
		add(new MountedMapper(
				"${account}/${depot}/pulls/${request}/compare", RequestComparePage.class));
		add(new MountedMapper(
				"${account}/${depot}/pulls/${request}/attachments", RequestAttachmentsPage.class));

		add(new MountedMapper("${account}/${depot}/settings/general", GeneralSettingPage.class));
		add(new MountedMapper("${account}/${depot}/settings/teams", DepotTeamListPage.class));
		add(new MountedMapper("${account}/${depot}/settings/collaborators", DepotCollaboratorListPage.class));
		add(new MountedMapper("${account}/${depot}/settings/effective-privilege", DepotEffectivePrivilegePage.class));
		add(new MountedMapper("${account}/${depot}/settings/gate-keeper", GateKeeperPage.class));
		add(new MountedMapper("${account}/${depot}/settings/integration-policy", IntegrationPolicyPage.class));
		
		add(new MountedMapper("${account}/${depot}/no-commits", NoCommitsPage.class));
	}

}
