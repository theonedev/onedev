package com.pmease.gitop.web;

import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.core.request.mapper.MountedMapper;
import org.apache.wicket.core.request.mapper.ResourceMapper;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.mapper.CompoundRequestMapper;
import org.apache.wicket.request.resource.ResourceReference;

import com.pmease.gitop.model.validation.RepositoryNameValidator;
import com.pmease.gitop.model.validation.UserNameValidator;
import com.pmease.gitop.web.common.wicket.mapper.PageParameterAwareMountedMapper;
import com.pmease.gitop.web.common.wicket.mapper.PatternMountedMapper;
import com.pmease.gitop.web.page.TestPage;
import com.pmease.gitop.web.page.account.RegisterPage;
import com.pmease.gitop.web.page.account.home.AccountHomePage;
import com.pmease.gitop.web.page.account.setting.members.AccountMembersSettingPage;
import com.pmease.gitop.web.page.account.setting.profile.AccountProfilePage;
import com.pmease.gitop.web.page.account.setting.repo.RepositoriesPage;
import com.pmease.gitop.web.page.account.setting.teams.AccountTeamsPage;
import com.pmease.gitop.web.page.account.setting.teams.AddTeamPage;
import com.pmease.gitop.web.page.account.setting.teams.EditTeamPage;
import com.pmease.gitop.web.page.admin.AdministrationOverviewPage;
import com.pmease.gitop.web.page.admin.LicensingPage;
import com.pmease.gitop.web.page.admin.MailSettingEdit;
import com.pmease.gitop.web.page.admin.SupportPage;
import com.pmease.gitop.web.page.admin.SystemSettingEdit;
import com.pmease.gitop.web.page.admin.UserAdministrationPage;
import com.pmease.gitop.web.page.error.AccessDeniedPage;
import com.pmease.gitop.web.page.error.InternalServerErrorPage;
import com.pmease.gitop.web.page.error.PageNotFoundPage;
import com.pmease.gitop.web.page.init.ServerInitPage;
import com.pmease.gitop.web.page.repository.pullrequest.ClosedRequestsPage;
import com.pmease.gitop.web.page.repository.pullrequest.NewRequestPage;
import com.pmease.gitop.web.page.repository.pullrequest.OpenRequestsPage;
import com.pmease.gitop.web.page.repository.pullrequest.RequestActivitiesPage;
import com.pmease.gitop.web.page.repository.pullrequest.RequestChangesPage;
import com.pmease.gitop.web.page.repository.pullrequest.RequestUpdatesPage;
import com.pmease.gitop.web.page.repository.settings.CreateRepositoryPage;
import com.pmease.gitop.web.page.repository.settings.GateKeeperSettingPage;
import com.pmease.gitop.web.page.repository.settings.PullRequestSettingsPage;
import com.pmease.gitop.web.page.repository.settings.RepositoryAuditLogPage;
import com.pmease.gitop.web.page.repository.settings.RepositoryHooksPage;
import com.pmease.gitop.web.page.repository.settings.RepositoryOptionsPage;
import com.pmease.gitop.web.page.repository.settings.RepositoryPermissionsPage;
import com.pmease.gitop.web.page.repository.source.RepositoryHomePage;
import com.pmease.gitop.web.page.repository.source.blame.BlobBlamePage;
import com.pmease.gitop.web.page.repository.source.blob.SourceBlobPage;
import com.pmease.gitop.web.page.repository.source.blob.renderer.RawBlobResourceReference;
import com.pmease.gitop.web.page.repository.source.branches.BranchesPage;
import com.pmease.gitop.web.page.repository.source.commit.SourceCommitPage;
import com.pmease.gitop.web.page.repository.source.commits.CommitsPage;
import com.pmease.gitop.web.page.repository.source.contributors.ContributorsPage;
import com.pmease.gitop.web.page.repository.source.tags.GitArchiveResourceReference;
import com.pmease.gitop.web.page.repository.source.tags.TagsPage;
import com.pmease.gitop.web.page.repository.source.tree.SourceTreePage;
import com.pmease.gitop.web.util.UrlUtils;

public class GitopMappings extends CompoundRequestMapper {

	public GitopMappings(WebApplication app) {
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
		addPage("administration", AdministrationOverviewPage.class);
		addPage("administration/users", UserAdministrationPage.class);
		addPage("administration/mail-settings", MailSettingEdit.class);
		addPage("administration/system-settings", SystemSettingEdit.class);
		addPage("administration/support", SupportPage.class);
		addPage("administration/licensing", LicensingPage.class);
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
		addPage("${user}/settings/repositories", RepositoriesPage.class);
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
				"${user}/${repo}/tree/${objectId}", SourceTreePage.class));
		add(new PageParameterAwareMountedMapper(
				"${user}/${repo}/blob/${objectId}", SourceBlobPage.class));
		add(new PageParameterAwareMountedMapper(
				"${user}/${repo}/blame/#{objectId}", BlobBlamePage.class));
		add(new PageParameterAwareMountedMapper(
				"${user}/${repo}/commit/${objectId}", SourceCommitPage.class));
		add(new PageParameterAwareMountedMapper(
				"${user}/${repo}/commits/#{objectId}", CommitsPage.class));

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
				RepositoryOptionsPage.class));
		add(new PageParameterAwareMountedMapper(
				"${user}/${repo}/settings/gate-keepers",
				GateKeeperSettingPage.class));
		add(new PageParameterAwareMountedMapper(
				"${user}/${repo}/settings/hooks", RepositoryHooksPage.class));
		add(new PageParameterAwareMountedMapper(
				"${user}/${repo}/settings/pull-requests",
				PullRequestSettingsPage.class));
		add(new PageParameterAwareMountedMapper(
				"${user}/${repo}/settings/audits", RepositoryAuditLogPage.class));
		add(new PageParameterAwareMountedMapper(
				"${user}/${repo}/settings/permissions",
				RepositoryPermissionsPage.class));
	}

	private void addResources() {
		// --------------------------------------------------------------------
		// system shared resources
		// --------------------------------------------------------------------
		addResource("raw/${user}/${repo}/${objectId}", new RawBlobResourceReference());
		addResource("archive/${user}/${repo}/${file}",new GitArchiveResourceReference());
	}

	private void addPage(String path, Class<? extends Page> page) {
		this.add(new PatternMountedMapper(path, page).setExact(true));
	}

	private void addResource(String path, ResourceReference resource) {
		add(new ResourceMapper(path, resource));
	}
}
