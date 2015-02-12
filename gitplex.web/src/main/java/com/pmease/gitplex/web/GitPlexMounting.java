package com.pmease.gitplex.web;

import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.core.request.mapper.MountedMapper;
import org.apache.wicket.core.request.mapper.ResourceMapper;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.mapper.CompoundRequestMapper;
import org.apache.wicket.request.resource.ResourceReference;

import com.pmease.gitplex.core.validation.RepositoryNameValidator;
import com.pmease.gitplex.core.validation.UserNameValidator;
import com.pmease.gitplex.web.common.wicket.mapper.PageParameterAwareMountedMapper;
import com.pmease.gitplex.web.common.wicket.mapper.PatternMountedMapper;
import com.pmease.gitplex.web.page.RunModePage;
import com.pmease.gitplex.web.page.TestPage;
import com.pmease.gitplex.web.page.account.AccountHomePage;
import com.pmease.gitplex.web.page.account.AccountNotificationsPage;
import com.pmease.gitplex.web.page.account.AccountProfilePage;
import com.pmease.gitplex.web.page.account.MemberSettingPage;
import com.pmease.gitplex.web.page.account.RegisterPage;
import com.pmease.gitplex.web.page.account.repository.NewRepositoryPage;
import com.pmease.gitplex.web.page.account.repository.RepositoriesPage;
import com.pmease.gitplex.web.page.account.team.AccountTeamsPage;
import com.pmease.gitplex.web.page.account.team.AddTeamPage;
import com.pmease.gitplex.web.page.account.team.EditTeamPage;
import com.pmease.gitplex.web.page.admin.MailSettingPage;
import com.pmease.gitplex.web.page.admin.QosSettingPage;
import com.pmease.gitplex.web.page.admin.SystemSettingPage;
import com.pmease.gitplex.web.page.error.AccessDeniedPage;
import com.pmease.gitplex.web.page.error.InternalServerErrorPage;
import com.pmease.gitplex.web.page.error.PageNotFoundPage;
import com.pmease.gitplex.web.page.init.ServerInitPage;
import com.pmease.gitplex.web.page.repository.NoCommitsPage;
import com.pmease.gitplex.web.page.repository.RepositoryHomePage;
import com.pmease.gitplex.web.page.repository.admin.GeneralSettingPage;
import com.pmease.gitplex.web.page.repository.admin.PermissionSettingPage;
import com.pmease.gitplex.web.page.repository.admin.gatekeeper.GateKeeperPage;
import com.pmease.gitplex.web.page.repository.admin.integrationpolicy.IntegrationPolicyPage;
import com.pmease.gitplex.web.page.repository.code.blob.RepoBlobPage;
import com.pmease.gitplex.web.page.repository.code.blob.renderer.RawBlobResourceReference;
import com.pmease.gitplex.web.page.repository.code.branches.BranchComparePage;
import com.pmease.gitplex.web.page.repository.code.branches.RepoBranchesPage;
import com.pmease.gitplex.web.page.repository.code.commit.RepoCommitPage;
import com.pmease.gitplex.web.page.repository.code.commits.RepoCommitsPage;
import com.pmease.gitplex.web.page.repository.code.contributors.ContributorsPage;
import com.pmease.gitplex.web.page.repository.code.tags.GitArchiveResourceReference;
import com.pmease.gitplex.web.page.repository.code.tree.RepoTreePage;
import com.pmease.gitplex.web.page.repository.pullrequest.ClosedRequestsPage;
import com.pmease.gitplex.web.page.repository.pullrequest.NewRequestPage;
import com.pmease.gitplex.web.page.repository.pullrequest.OpenRequestsPage;
import com.pmease.gitplex.web.page.repository.pullrequest.RequestComparePage;
import com.pmease.gitplex.web.page.repository.pullrequest.RequestOverviewPage;
import com.pmease.gitplex.web.page.repository.pullrequest.RequestUpdatesPage;
import com.pmease.gitplex.web.util.UrlUtils;

public class GitPlexMounting extends CompoundRequestMapper {

	public GitPlexMounting(WebApplication app) {
		addErrorPages();
		addAdministrationPages();
		addAccountPages();
		addRepositoryPages();
		addResources();
		
		addPage("init", ServerInitPage.class);
		addPage("register", RegisterPage.class);
		addPage("/test", TestPage.class);
		addPage("run-mode", RunModePage.class);
	}

	private void addErrorPages() {
		addPage("404", PageNotFoundPage.class);
		addPage("403", AccessDeniedPage.class);
		addPage("501", InternalServerErrorPage.class);
	}

	private void addAdministrationPages() {
		addPage("administration/mail-settings", MailSettingPage.class);
		addPage("administration/system-settings", SystemSettingPage.class);
		addPage("administration/qos-settings", QosSettingPage.class);
	}

	private void addAccountPages() {
		add(new MountedMapper("${user}", AccountHomePage.class) {

			@Override
			protected boolean urlStartsWith(Url url, String... segments) {
				List<String> normalizedSegments = UrlUtils
						.normalizeUrlSegments(url.getSegments());
				if (normalizedSegments.size() < 1)
					return false;
				String userName = normalizedSegments.get(0);
				return !UserNameValidator.getReservedNames().contains(userName);
			}

		});

		addPage("${user}/notifications", AccountNotificationsPage.class);

		// account settings
		addPage("${user}/settings", AccountProfilePage.class);
		addPage("${user}/settings/repositories", RepositoriesPage.class);
		addPage("${user}/settings/members", MemberSettingPage.class);
		addPage("${user}/settings/teams", AccountTeamsPage.class);
		addPage("${user}/settings/teams/new", AddTeamPage.class);
		addPage("${user}/settings/teams/${teamId}", EditTeamPage.class);
	}

	private void addRepositoryPages() {
		add(new MountedMapper("${user}/${repo}", RepositoryHomePage.class) {

			@Override
			protected boolean urlStartsWith(Url url, String... segments) {
				List<String> normalizedSegments = UrlUtils
						.normalizeUrlSegments(url.getSegments());
				if (normalizedSegments.size() < 2)
					return false;
				String userName = normalizedSegments.get(0);
				if (UserNameValidator.getReservedNames().contains(userName))
					return false;

				String repositoryName = normalizedSegments.get(1);
				return !RepositoryNameValidator.getReservedNames().contains(
						repositoryName);
			}

		});

		// create repository
		addPage("new", NewRepositoryPage.class);

		add(new PageParameterAwareMountedMapper(
				"${user}/${repo}/tree", RepoTreePage.class));
		add(new PageParameterAwareMountedMapper(
				"${user}/${repo}/blob", RepoBlobPage.class));
		add(new PageParameterAwareMountedMapper(
				"${user}/${repo}/commit", RepoCommitPage.class));
		add(new PageParameterAwareMountedMapper(
				"${user}/${repo}/commits", RepoCommitsPage.class));
		add(new PageParameterAwareMountedMapper(
				"${user}/${repo}/compare", BranchComparePage.class));

		add(new PageParameterAwareMountedMapper("${user}/${repo}/branches",
				RepoBranchesPage.class));

		add(new PageParameterAwareMountedMapper(
				"${user}/${repo}/pull-requests/open", OpenRequestsPage.class));
		add(new PageParameterAwareMountedMapper(
				"${user}/${repo}/pull-requests/closed",
				ClosedRequestsPage.class));
		add(new PageParameterAwareMountedMapper(
				"${user}/${repo}/pull-requests/new", NewRequestPage.class));
		add(new PageParameterAwareMountedMapper(
				"${user}/${repo}/pull-requests/${request}", RequestOverviewPage.class));
		add(new PageParameterAwareMountedMapper(
				"${user}/${repo}/pull-requests/${request}/overview", RequestOverviewPage.class));
		add(new PageParameterAwareMountedMapper(
				"${user}/${repo}/pull-requests/${request}/updates", RequestUpdatesPage.class));
		add(new PageParameterAwareMountedMapper(
				"${user}/${repo}/pull-requests/${request}/compare", RequestComparePage.class));

		add(new PageParameterAwareMountedMapper("${user}/${repo}/contributors",
				ContributorsPage.class));

		add(new PageParameterAwareMountedMapper("${user}/${repo}/settings",
				GeneralSettingPage.class));
		add(new PageParameterAwareMountedMapper(
				"${user}/${repo}/settings/gate-keepers",
				GateKeeperPage.class));
		add(new PageParameterAwareMountedMapper(
				"${user}/${repo}/settings/integration-setting",
				IntegrationPolicyPage.class));
		add(new PageParameterAwareMountedMapper(
				"${user}/${repo}/settings/permissions",
				PermissionSettingPage.class));
		
		add(new PageParameterAwareMountedMapper(
				"${user}/${repo}/no-commits", NoCommitsPage.class));
	}

	private void addResources() {
		// --------------------------------------------------------------------
		// system shared resources
		// --------------------------------------------------------------------
		addResource("raw/${user}/${repo}", new RawBlobResourceReference());
		addResource("archive/${user}/${repo}",new GitArchiveResourceReference());
	}

	private void addPage(String path, Class<? extends Page> page) {
		add(new PatternMountedMapper(path, page).setExact(true));
	}

	private void addResource(String path, ResourceReference resource) {
		add(new ResourceMapper(path, resource));
	}
}
