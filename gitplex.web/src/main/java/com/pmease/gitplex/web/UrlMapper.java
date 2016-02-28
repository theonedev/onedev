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
import com.pmease.gitplex.core.util.validation.DepotNameValidator;
import com.pmease.gitplex.core.util.validation.AccountNameValidator;
import com.pmease.gitplex.web.page.account.depots.AccountDepotsPage;
import com.pmease.gitplex.web.page.account.depots.NewAccountDepotPage;
import com.pmease.gitplex.web.page.account.notifications.AccountNotificationsPage;
import com.pmease.gitplex.web.page.account.setting.AvatarEditPage;
import com.pmease.gitplex.web.page.account.setting.PasswordEditPage;
import com.pmease.gitplex.web.page.account.setting.ProfileEditPage;
import com.pmease.gitplex.web.page.admin.AccountListPage;
import com.pmease.gitplex.web.page.admin.MailSettingPage;
import com.pmease.gitplex.web.page.admin.NewAccountPage;
import com.pmease.gitplex.web.page.admin.QosSettingPage;
import com.pmease.gitplex.web.page.admin.SystemSettingPage;
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
		
		add(new MountedMapper("/test", TestPage.class));
		add(new MountedMapper("runmode", RunModePage.class));
		
		addResources();
	}

	private void addResources() {
		add(new ResourceMapper("${user}/${depot}/archive", new ArchiveResourceReference()) {

			@Override
			public int getCompatibilityScore(Request request) {
				return 3;
			}
			
		});
		add(new ResourceMapper("${user}/${depot}/raw", new BlobResourceReference()) {

			@Override
			public int getCompatibilityScore(Request request) {
				return 3;
			}
			
		});
		add(new ResourceMapper("${user}/${depot}/pulls/${request}/attachments/${attachment}", new AttachmentResourceReference()) {

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
		add(new MountedMapper("administration/accounts/new", NewAccountPage.class));
		add(new MountedMapper("administration/mail-setting", MailSettingPage.class));
		add(new MountedMapper("administration/system-setting", SystemSettingPage.class));
		add(new MountedMapper("administration/qos-setting", QosSettingPage.class));
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
		add(new MountedMapper("${user}", AccountDepotsPage.class) {

			@Override
			protected boolean urlStartsWith(Url url, String... segments) {
				List<String> urlSegments = normalize(url.getSegments());
				if (urlSegments.size() < 1)
					return false;
				String userName = urlSegments.get(0);
				
				return !AccountNameValidator.getReservedNames().contains(userName);
			}

		});
		
		add(new MountedMapper("${user}/depots", AccountDepotsPage.class));
		add(new MountedMapper("${user}/depots/new", NewAccountDepotPage.class));
		add(new MountedMapper("${user}/notifications", AccountNotificationsPage.class));
		add(new MountedMapper("${user}/setting/profile", ProfileEditPage.class));
		add(new MountedMapper("${user}/setting/avatar", AvatarEditPage.class));
		add(new MountedMapper("${user}/setting/password", PasswordEditPage.class));
	}

	private void addDepotPages() {
		add(new MountedMapper("${user}/${depot}", DepotFilePage.class) {

			@Override
			protected boolean urlStartsWith(Url url, String... segments) {
				List<String> urlSegments = normalize(url.getSegments());
				if (urlSegments.size() < 2)
					return false;
				String userName = urlSegments.get(0);
				if (AccountNameValidator.getReservedNames().contains(userName))
					return false;

				String depotName = urlSegments.get(1);
				return !DepotNameValidator.getReservedNames().contains(depotName);
			}

		});

//		add(new ParameterAwareMountedMapper("${user}/${depot}/browse", RepoFilePage.class));
		add(new MountedMapper("${user}/${depot}/commit", CommitDetailPage.class));
		add(new MountedMapper("${user}/${depot}/commits", DepotCommitsPage.class));
		add(new MountedMapper("${user}/${depot}/compare", RevisionComparePage.class));

		add(new MountedMapper("${user}/${depot}/branches", DepotBranchesPage.class));
		add(new MountedMapper("${user}/${depot}/tags", DepotTagsPage.class));

		add(new MountedMapper("${user}/${depot}/pulls", RequestListPage.class));
		add(new MountedMapper("${user}/${depot}/pulls/new", NewRequestPage.class));
		add(new MountedMapper("${user}/${depot}/pulls/${request}", RequestOverviewPage.class));
		add(new MountedMapper(
				"${user}/${depot}/pulls/${request}/overview", RequestOverviewPage.class));
		add(new MountedMapper(
				"${user}/${depot}/pulls/${request}/updates", RequestUpdatesPage.class));
		add(new MountedMapper(
				"${user}/${depot}/pulls/${request}/compare", RequestComparePage.class));
		add(new MountedMapper(
				"${user}/${depot}/pulls/${request}/attachments", RequestAttachmentsPage.class));

		add(new MountedMapper("${user}/${depot}/setting", GeneralSettingPage.class));
		add(new MountedMapper("${user}/${depot}/setting/general", GeneralSettingPage.class));
		add(new MountedMapper("${user}/${depot}/setting/gate-keeper", GateKeeperPage.class));
		add(new MountedMapper("${user}/${depot}/setting/integration-policy", IntegrationPolicyPage.class));
		
		add(new MountedMapper("${user}/${depot}/no-commits", NoCommitsPage.class));
	}

}
