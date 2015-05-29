package com.pmease.gitplex.web.mapper;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.core.request.mapper.MountedMapper;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.mapper.CompoundRequestMapper;

import com.pmease.commons.util.StringUtils;
import com.pmease.gitplex.core.validation.RepositoryNameValidator;
import com.pmease.gitplex.core.validation.UserNameValidator;
import com.pmease.gitplex.web.page.account.notifications.AccountNotificationsPage;
import com.pmease.gitplex.web.page.account.repositories.AccountReposPage;
import com.pmease.gitplex.web.page.account.repositories.NewAccountRepoPage;
import com.pmease.gitplex.web.page.account.setting.AvatarEditPage;
import com.pmease.gitplex.web.page.account.setting.PasswordEditPage;
import com.pmease.gitplex.web.page.account.setting.ProfileEditPage;
import com.pmease.gitplex.web.page.home.accounts.NewAccountPage;
import com.pmease.gitplex.web.page.home.admin.MailSettingPage;
import com.pmease.gitplex.web.page.home.admin.QosSettingPage;
import com.pmease.gitplex.web.page.home.admin.SystemSettingPage;
import com.pmease.gitplex.web.page.home.repositories.RepositoriesPage;
import com.pmease.gitplex.web.page.init.ServerInitPage;
import com.pmease.gitplex.web.page.repository.NoCommitsPage;
import com.pmease.gitplex.web.page.repository.branches.BranchComparePage;
import com.pmease.gitplex.web.page.repository.branches.RepoBranchesPage;
import com.pmease.gitplex.web.page.repository.commit.RepoCommitPage;
import com.pmease.gitplex.web.page.repository.commit.RepoCommitsPage;
import com.pmease.gitplex.web.page.repository.file.RepoFilePage;
import com.pmease.gitplex.web.page.repository.pullrequest.OpenRequestsPage;
import com.pmease.gitplex.web.page.repository.pullrequest.RequestComparePage;
import com.pmease.gitplex.web.page.repository.pullrequest.RequestOverviewPage;
import com.pmease.gitplex.web.page.repository.pullrequest.RequestUpdatesPage;
import com.pmease.gitplex.web.page.repository.setting.gatekeeper.GateKeeperPage;
import com.pmease.gitplex.web.page.repository.setting.general.GeneralSettingPage;
import com.pmease.gitplex.web.page.repository.setting.integrationpolicy.IntegrationPolicyPage;
import com.pmease.gitplex.web.page.repository.tags.RepoTagsPage;
import com.pmease.gitplex.web.page.security.ForgetPage;
import com.pmease.gitplex.web.page.security.LoginPage;
import com.pmease.gitplex.web.page.security.LogoutPage;
import com.pmease.gitplex.web.page.security.RegisterPage;
import com.pmease.gitplex.web.page.test.RunModePage;
import com.pmease.gitplex.web.page.test.TestPage;

public class RootMapper extends CompoundRequestMapper {

	public RootMapper(WebApplication app) {
		addAdministrationPages();
		addAccountPages();
		addRepoPages();
		
		addPage("init", ServerInitPage.class);
		addPage("login", LoginPage.class);
		addPage("logout", LogoutPage.class);
		addPage("register", RegisterPage.class);
		addPage("forget", ForgetPage.class);
		
		addPage("/test", TestPage.class);
		addPage("run-mode", RunModePage.class);
	}

	private void addAdministrationPages() {
		addPage("administration/mail-setting", MailSettingPage.class);
		addPage("administration/system-setting", SystemSettingPage.class);
		addPage("administration/qos-setting", QosSettingPage.class);
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
		addPage("accounts/new", NewAccountPage.class);
		
		add(new MountedMapper("${user}", AccountReposPage.class) {

			@Override
			protected boolean urlStartsWith(Url url, String... segments) {
				List<String> urlSegments = normalize(url.getSegments());
				if (urlSegments.size() < 1)
					return false;
				String userName = urlSegments.get(0);
				
				return !UserNameValidator.getReservedNames().contains(userName);
			}

		});
		
		addPage("${user}/repositories", AccountReposPage.class);
		addPage("${user}/repositories/new", NewAccountRepoPage.class);
		addPage("${user}/notifications", AccountNotificationsPage.class);
		addPage("${user}/setting/profile", ProfileEditPage.class);
		addPage("${user}/setting/avatar", AvatarEditPage.class);
		addPage("${user}/setting/password", PasswordEditPage.class);
	}

	private void addRepoPages() {
		addPage("repositories", RepositoriesPage.class);
		
		add(new MountedMapper("${user}/${repo}", RepoFilePage.class) {

			@Override
			protected boolean urlStartsWith(Url url, String... segments) {
				List<String> urlSegments = normalize(url.getSegments());
				if (urlSegments.size() < 2)
					return false;
				String userName = urlSegments.get(0);
				if (UserNameValidator.getReservedNames().contains(userName))
					return false;

				String repositoryName = urlSegments.get(1);
				return !RepositoryNameValidator.getReservedNames().contains(repositoryName);
			}

		});

		add(new PageParameterAwareMountedMapper("${user}/${repo}/file", RepoFilePage.class));
		add(new PageParameterAwareMountedMapper("${user}/${repo}/commit", RepoCommitPage.class));
		add(new PageParameterAwareMountedMapper("${user}/${repo}/commits", RepoCommitsPage.class));
		add(new PageParameterAwareMountedMapper("${user}/${repo}/compare", BranchComparePage.class));

		add(new PageParameterAwareMountedMapper("${user}/${repo}/branches", RepoBranchesPage.class));
		add(new PageParameterAwareMountedMapper("${user}/${repo}/tags", RepoTagsPage.class));

		add(new PageParameterAwareMountedMapper(
				"${user}/${repo}/pull-requests", OpenRequestsPage.class));
		add(new PageParameterAwareMountedMapper(
				"${user}/${repo}/pull-requests/${request}", RequestOverviewPage.class));
		add(new PageParameterAwareMountedMapper(
				"${user}/${repo}/pull-requests/${request}/overview", RequestOverviewPage.class));
		add(new PageParameterAwareMountedMapper(
				"${user}/${repo}/pull-requests/${request}/updates", RequestUpdatesPage.class));
		add(new PageParameterAwareMountedMapper(
				"${user}/${repo}/pull-requests/${request}/compare", RequestComparePage.class));

		add(new PageParameterAwareMountedMapper("${user}/${repo}/setting", GeneralSettingPage.class));
		add(new PageParameterAwareMountedMapper("${user}/${repo}/setting/general", GeneralSettingPage.class));
		add(new PageParameterAwareMountedMapper("${user}/${repo}/setting/gate-keeper", GateKeeperPage.class));
		add(new PageParameterAwareMountedMapper("${user}/${repo}/setting/integration-policy", IntegrationPolicyPage.class));
		
		add(new PageParameterAwareMountedMapper("${user}/${repo}/no-commits", NoCommitsPage.class));
	}

	private void addPage(String path, Class<? extends Page> page) {
		add(new PatternMountedMapper(path, page).setExact(true));
	}

}
