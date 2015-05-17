package com.pmease.gitplex.web.mapper;

import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.core.request.mapper.MountedMapper;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.mapper.CompoundRequestMapper;

import com.pmease.gitplex.core.validation.RepositoryNameValidator;
import com.pmease.gitplex.core.validation.UserNameValidator;
import com.pmease.gitplex.web.page.account.list.AccountListPage;
import com.pmease.gitplex.web.page.account.list.NewAccountPage;
import com.pmease.gitplex.web.page.account.notification.AccountNotificationPage;
import com.pmease.gitplex.web.page.account.overview.AccountOverviewPage;
import com.pmease.gitplex.web.page.account.setting.AvatarEditPage;
import com.pmease.gitplex.web.page.account.setting.PasswordEditPage;
import com.pmease.gitplex.web.page.account.setting.ProfileEditPage;
import com.pmease.gitplex.web.page.admin.MailSettingPage;
import com.pmease.gitplex.web.page.admin.QosSettingPage;
import com.pmease.gitplex.web.page.admin.SystemSettingPage;
import com.pmease.gitplex.web.page.init.ServerInitPage;
import com.pmease.gitplex.web.page.repository.NoCommitsPage;
import com.pmease.gitplex.web.page.repository.admin.GeneralSettingPage;
import com.pmease.gitplex.web.page.repository.admin.gatekeeper.GateKeeperPage;
import com.pmease.gitplex.web.page.repository.admin.integrationpolicy.IntegrationPolicyPage;
import com.pmease.gitplex.web.page.repository.code.blob.RepoBlobPage;
import com.pmease.gitplex.web.page.repository.code.branches.BranchComparePage;
import com.pmease.gitplex.web.page.repository.code.branches.RepoBranchesPage;
import com.pmease.gitplex.web.page.repository.code.tree.RepoTreePage;
import com.pmease.gitplex.web.page.repository.commit.RepoCommitPage;
import com.pmease.gitplex.web.page.repository.commit.RepoCommitsPage;
import com.pmease.gitplex.web.page.repository.list.RepoListPage;
import com.pmease.gitplex.web.page.repository.overview.RepoOverviewPage;
import com.pmease.gitplex.web.page.repository.pullrequest.ClosedRequestsPage;
import com.pmease.gitplex.web.page.repository.pullrequest.NewRequestPage;
import com.pmease.gitplex.web.page.repository.pullrequest.OpenRequestsPage;
import com.pmease.gitplex.web.page.repository.pullrequest.RequestComparePage;
import com.pmease.gitplex.web.page.repository.pullrequest.RequestOverviewPage;
import com.pmease.gitplex.web.page.repository.pullrequest.RequestUpdatesPage;
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
		addPage("administration/mail-settings", MailSettingPage.class);
		addPage("administration/system-settings", SystemSettingPage.class);
		addPage("administration/qos-settings", QosSettingPage.class);
	}

	private void addAccountPages() {
		addPage("accounts", AccountListPage.class);
		addPage("accounts/new", NewAccountPage.class);
		
		add(new MountedMapper("${user}", AccountOverviewPage.class) {

			@Override
			protected boolean urlStartsWith(Url url, String... segments) {
				List<String> urlSegments = url.getSegments();
				if (urlSegments.size() < 1)
					return false;
				String userName = urlSegments.get(0);
				
				return !UserNameValidator.getReservedNames().contains(userName);
			}

		});

		addPage("${user}/notification", AccountNotificationPage.class);
		
		addPage("${user}/setting/profile", ProfileEditPage.class);
		addPage("${user}/setting/avatar", AvatarEditPage.class);
		addPage("${user}/setting/password", PasswordEditPage.class);
	}

	private void addRepoPages() {
		addPage("repositories", RepoListPage.class);
		
		add(new MountedMapper("${user}/${repo}", RepoOverviewPage.class) {

			@Override
			protected boolean urlStartsWith(Url url, String... segments) {
				List<String> urlSegments = url.getSegments();
				if (urlSegments.size() < 2)
					return false;
				String userName = urlSegments.get(0);
				if (UserNameValidator.getReservedNames().contains(userName))
					return false;

				String repositoryName = urlSegments.get(1);
				return !RepositoryNameValidator.getReservedNames().contains(
						repositoryName);
			}

		});

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

		add(new PageParameterAwareMountedMapper("${user}/${repo}/settings",
				GeneralSettingPage.class));
		add(new PageParameterAwareMountedMapper(
				"${user}/${repo}/settings/gate-keepers",
				GateKeeperPage.class));
		add(new PageParameterAwareMountedMapper(
				"${user}/${repo}/settings/integration-setting",
				IntegrationPolicyPage.class));
		
		add(new PageParameterAwareMountedMapper(
				"${user}/${repo}/no-commits", NoCommitsPage.class));
	}

	private void addPage(String path, Class<? extends Page> page) {
		add(new PatternMountedMapper(path, page).setExact(true));
	}

}
