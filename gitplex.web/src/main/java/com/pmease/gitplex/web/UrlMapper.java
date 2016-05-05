package com.pmease.gitplex.web;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.core.request.mapper.ResourceMapper;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.mapper.CompoundRequestMapper;

import com.pmease.commons.util.StringUtils;
import com.pmease.commons.wicket.NoVersionMountedMapper;
import com.pmease.gitplex.core.util.validation.AccountNameValidator;
import com.pmease.gitplex.core.util.validation.DepotNameValidator;
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
import com.pmease.gitplex.web.page.depot.NoCommitsPage;
import com.pmease.gitplex.web.page.depot.branches.DepotBranchesPage;
import com.pmease.gitplex.web.page.depot.commit.CommitDetailPage;
import com.pmease.gitplex.web.page.depot.commit.DepotCommitsPage;
import com.pmease.gitplex.web.page.depot.compare.RevisionComparePage;
import com.pmease.gitplex.web.page.depot.file.DepotFilePage;
import com.pmease.gitplex.web.page.depot.overview.DepotOverviewPage;
import com.pmease.gitplex.web.page.depot.pullrequest.newrequest.NewRequestPage;
import com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.attachments.RequestAttachmentsPage;
import com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.compare.RequestComparePage;
import com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.overview.RequestOverviewPage;
import com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.updates.RequestUpdatesPage;
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
		add(new ResourceMapper("${account}/${depot}/archive", new ArchiveResourceReference()) {

			@Override
			public int getCompatibilityScore(Request request) {
				if (isDepotUrl(request.getUrl()))
					return 3;
				else
					return 0;
			}
			
		});
		add(new ResourceMapper("${account}/${depot}/raw", new BlobResourceReference()) {

			@Override
			public int getCompatibilityScore(Request request) {
				if (isDepotUrl(request.getUrl()))
					return 3;
				else
					return 0;
			}
			
		});
		add(new ResourceMapper("${account}/${depot}/pulls/${request}/attachments/${attachment}", 
				new AttachmentResourceReference()) {

			@Override
			public int getCompatibilityScore(Request request) {
				if (isDepotUrl(request.getUrl()))
					return 8;
				else
					return 0;
			}
			
		});
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
	
	public List<String> normalize(List<String> urlSegments) {
		List<String> normalized = new ArrayList<String>();
		for (String each: urlSegments) {
			each = StringUtils.remove(each, '/');
			if (each.length() != 0)
				normalized.add(each);
		}
		return normalized;
	}

	private boolean isAccountUrl(Url url) {
		List<String> urlSegments = normalize(url.getSegments());
		if (urlSegments.size() < 1)
			return false;
		String accountName = urlSegments.get(0);
		
		return !AccountNameValidator.getReservedNames().contains(accountName);
	}
	
	private void addAccountPages() {
		add(new NoVersionMountedMapper("${account}", AccountOverviewPage.class) {

			@Override
			protected boolean urlStartsWith(Url url, String... segments) {
				return isAccountUrl(url);
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

	private boolean isDepotUrl(Url url) {
		List<String> urlSegments = normalize(url.getSegments());
		if (urlSegments.size() < 2)
			return false;
		
		String accountName = urlSegments.get(0);
		if (AccountNameValidator.getReservedNames().contains(accountName))
			return false;

		String depotName = urlSegments.get(1);
		return !DepotNameValidator.getReservedNames().contains(depotName);
	}
	
	private void addDepotPages() {
		add(new NoVersionMountedMapper("${account}/${depot}", DepotOverviewPage.class) {

			@Override
			protected boolean urlStartsWith(Url url, String... segments) {
				return isDepotUrl(url);
			}

		});

		add(new NoVersionMountedMapper("${account}/${depot}/files", DepotFilePage.class));
		add(new NoVersionMountedMapper("${account}/${depot}/commit", CommitDetailPage.class));
		add(new NoVersionMountedMapper("${account}/${depot}/commits", DepotCommitsPage.class));
		add(new NoVersionMountedMapper("${account}/${depot}/compare", RevisionComparePage.class));

		add(new NoVersionMountedMapper("${account}/${depot}/branches", DepotBranchesPage.class));
		add(new NoVersionMountedMapper("${account}/${depot}/tags", DepotTagsPage.class));

		add(new NoVersionMountedMapper("${account}/${depot}/pulls", RequestListPage.class));
		add(new NoVersionMountedMapper("${account}/${depot}/pulls/new", NewRequestPage.class));
		add(new NoVersionMountedMapper("${account}/${depot}/pulls/${request}", RequestOverviewPage.class));
		add(new NoVersionMountedMapper(
				"${account}/${depot}/pulls/${request}/overview", RequestOverviewPage.class));
		add(new NoVersionMountedMapper(
				"${account}/${depot}/pulls/${request}/updates", RequestUpdatesPage.class));
		add(new NoVersionMountedMapper(
				"${account}/${depot}/pulls/${request}/compare", RequestComparePage.class));
		add(new NoVersionMountedMapper(
				"${account}/${depot}/pulls/${request}/attachments", RequestAttachmentsPage.class));

		add(new NoVersionMountedMapper("${account}/${depot}/settings/general", GeneralSettingPage.class));
		add(new NoVersionMountedMapper("${account}/${depot}/settings/teams", DepotTeamListPage.class));
		add(new NoVersionMountedMapper("${account}/${depot}/settings/collaborators", DepotCollaboratorListPage.class));
		add(new NoVersionMountedMapper("${account}/${depot}/settings/effective-privilege", DepotEffectivePrivilegePage.class));
		add(new NoVersionMountedMapper("${account}/${depot}/settings/gate-keeper", GateKeeperPage.class));
		add(new NoVersionMountedMapper("${account}/${depot}/settings/integration-policy", IntegrationPolicyPage.class));
		
		add(new NoVersionMountedMapper("${account}/${depot}/no-commits", NoCommitsPage.class));
	}

}
