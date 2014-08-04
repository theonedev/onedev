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
import com.pmease.gitplex.web.page.TestPage;
import com.pmease.gitplex.web.page.account.RegisterPage;
import com.pmease.gitplex.web.page.account.home.AccountHomePage;
import com.pmease.gitplex.web.page.account.setting.members.AccountMembersSettingPage;
import com.pmease.gitplex.web.page.account.setting.profile.AccountProfilePage;
import com.pmease.gitplex.web.page.account.setting.repo.AccountRepositoriesPage;
import com.pmease.gitplex.web.page.account.setting.teams.AccountTeamsPage;
import com.pmease.gitplex.web.page.account.setting.teams.AddTeamPage;
import com.pmease.gitplex.web.page.account.setting.teams.EditTeamPage;
import com.pmease.gitplex.web.page.admin.MailSettingPage;
import com.pmease.gitplex.web.page.admin.SystemSettingPage;
import com.pmease.gitplex.web.page.error.AccessDeniedPage;
import com.pmease.gitplex.web.page.error.InternalServerErrorPage;
import com.pmease.gitplex.web.page.error.PageNotFoundPage;
import com.pmease.gitplex.web.page.init.ServerInitPage;
import com.pmease.gitplex.web.page.repository.NoCommitsPage;
import com.pmease.gitplex.web.page.repository.RepositoryHomePage;
import com.pmease.gitplex.web.page.repository.admin.CreateRepositoryPage;
import com.pmease.gitplex.web.page.repository.admin.GateKeeperSettingPage;
import com.pmease.gitplex.web.page.repository.admin.IntegrationSettingPage;
import com.pmease.gitplex.web.page.repository.admin.RepoAuditPage;
import com.pmease.gitplex.web.page.repository.admin.RepoHooksPage;
import com.pmease.gitplex.web.page.repository.admin.RepoOptionsPage;
import com.pmease.gitplex.web.page.repository.admin.RepoPermissionsPage;
import com.pmease.gitplex.web.page.repository.info.code.blame.BlobBlamePage;
import com.pmease.gitplex.web.page.repository.info.code.blob.RepoBlobPage;
import com.pmease.gitplex.web.page.repository.info.code.blob.renderer.RawBlobResourceReference;
import com.pmease.gitplex.web.page.repository.info.code.branches.BranchesPage;
import com.pmease.gitplex.web.page.repository.info.code.commit.RepoCommitPage;
import com.pmease.gitplex.web.page.repository.info.code.commits.RepoCommitsPage;
import com.pmease.gitplex.web.page.repository.info.code.contributors.ContributorsPage;
import com.pmease.gitplex.web.page.repository.info.code.tags.GitArchiveResourceReference;
import com.pmease.gitplex.web.page.repository.info.code.tags.TagsPage;
import com.pmease.gitplex.web.page.repository.info.code.tree.RepoTreePage;
import com.pmease.gitplex.web.page.repository.info.pullrequest.ClosedRequestsPage;
import com.pmease.gitplex.web.page.repository.info.pullrequest.NewRequestPage;
import com.pmease.gitplex.web.page.repository.info.pullrequest.OpenRequestsPage;
import com.pmease.gitplex.web.page.repository.info.pullrequest.RequestActivitiesPage;
import com.pmease.gitplex.web.page.repository.info.pullrequest.RequestChangesPage;
import com.pmease.gitplex.web.page.repository.info.pullrequest.RequestUpdatesPage;
import com.pmease.gitplex.web.util.UrlUtils;

public class GitPlexMappings extends CompoundRequestMapper {

	public GitPlexMappings(WebApplication app) {
		addSystemPages();
		addAdministrationPages();
		addAccountPages();
		addRepositoryPages();
		addTestPages();

		addResources();
	}

	private void addTestPages() {
		addPage("/test", TestPage.class);
	}
	
	private void addSystemPages() {
		// --------------------------------------------------------------------
		// system global pages
		// --------------------------------------------------------------------
		addPage("init", ServerInitPage.class);
		addPage("register", RegisterPage.class);

		// --------------------------------------------------------------------
		// system errors page
		// --------------------------------------------------------------------
		addPage("404", PageNotFoundPage.class);
		addPage("403", AccessDeniedPage.class);
		addPage("501", InternalServerErrorPage.class);
	}

	private void addAdministrationPages() {
		addPage("administration/mail-settings", MailSettingPage.class);
		addPage("administration/system-settings", SystemSettingPage.class);
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

		// account settings
		addPage("${user}/settings", AccountProfilePage.class);
		addPage("${user}/settings/repositories", AccountRepositoriesPage.class);
		addPage("${user}/settings/members", AccountMembersSettingPage.class);
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
		addPage("new", CreateRepositoryPage.class);

		add(new PageParameterAwareMountedMapper(
				"${user}/${repo}/tree", RepoTreePage.class));
		add(new PageParameterAwareMountedMapper(
				"${user}/${repo}/blob", RepoBlobPage.class));
		add(new PageParameterAwareMountedMapper(
				"${user}/${repo}/blame", BlobBlamePage.class));
		add(new PageParameterAwareMountedMapper(
				"${user}/${repo}/commit", RepoCommitPage.class));
		add(new PageParameterAwareMountedMapper(
				"${user}/${repo}/commits", RepoCommitsPage.class));

		add(new PageParameterAwareMountedMapper("${user}/${repo}/branches",
				BranchesPage.class));
		add(new PageParameterAwareMountedMapper("${user}/${repo}/tags",
				TagsPage.class));

		add(new PageParameterAwareMountedMapper(
				"${user}/${repo}/pull-requests/open", OpenRequestsPage.class));
		add(new PageParameterAwareMountedMapper(
				"${user}/${repo}/pull-requests/closed",
				ClosedRequestsPage.class));
		add(new PageParameterAwareMountedMapper(
				"${user}/${repo}/pull-requests/new", NewRequestPage.class));
		add(new PageParameterAwareMountedMapper(
				"${user}/${repo}/pull-requests/${request}", RequestActivitiesPage.class));
		add(new PageParameterAwareMountedMapper(
				"${user}/${repo}/pull-requests/${request}/activities", RequestActivitiesPage.class));
		add(new PageParameterAwareMountedMapper(
				"${user}/${repo}/pull-requests/${request}/updates", RequestUpdatesPage.class));
		add(new PageParameterAwareMountedMapper(
				"${user}/${repo}/pull-requests/${request}/changes", RequestChangesPage.class));

		add(new PageParameterAwareMountedMapper("${user}/${repo}/contributors",
				ContributorsPage.class));

		add(new PageParameterAwareMountedMapper("${user}/${repo}/settings",
				RepoOptionsPage.class));
		add(new PageParameterAwareMountedMapper(
				"${user}/${repo}/settings/gate-keepers",
				GateKeeperSettingPage.class));
		add(new PageParameterAwareMountedMapper(
				"${user}/${repo}/settings/integration-setting",
				IntegrationSettingPage.class));
		add(new PageParameterAwareMountedMapper(
				"${user}/${repo}/settings/hooks", RepoHooksPage.class));
		add(new PageParameterAwareMountedMapper(
				"${user}/${repo}/settings/audits", RepoAuditPage.class));
		add(new PageParameterAwareMountedMapper(
				"${user}/${repo}/settings/permissions",
				RepoPermissionsPage.class));
		
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
		this.add(new PatternMountedMapper(path, page).setExact(true));
	}

	private void addResource(String path, ResourceReference resource) {
		add(new ResourceMapper(path, resource));
	}
}
