package com.pmease.gitplex.web.page.repository.setting;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.commons.wicket.component.tabbable.PageTab;
import com.pmease.commons.wicket.component.tabbable.Tabbable;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.core.security.SecurityUtils;
import com.pmease.gitplex.web.component.confirmdelete.ConfirmDeleteRepoModal;
import com.pmease.gitplex.web.component.confirmdelete.ConfirmDeleteRepoModalBehavior;
import com.pmease.gitplex.web.page.account.AccountPage;
import com.pmease.gitplex.web.page.account.repositories.AccountReposPage;
import com.pmease.gitplex.web.page.repository.RepositoryPage;
import com.pmease.gitplex.web.page.repository.setting.gatekeeper.GateKeeperPage;
import com.pmease.gitplex.web.page.repository.setting.general.GeneralSettingPage;
import com.pmease.gitplex.web.page.repository.setting.integrationpolicy.IntegrationPolicyPage;

@SuppressWarnings("serial")
public class RepoSettingPage extends RepositoryPage {

	public RepoSettingPage(PageParameters params) {
		super(params);
	}

	@Override
	protected boolean isPermitted() {
		return SecurityUtils.canManage(getRepository());
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		List<PageTab> tabs = new ArrayList<>();
		tabs.add(new RepoSettingTab("General Setting", "fa fa-fw fa-pencil", GeneralSettingPage.class));
		tabs.add(new RepoSettingTab("Gate Keeper", "fa fa-ext fa-fw fa-gatekeeper", GateKeeperPage.class));
		tabs.add(new RepoSettingTab("Integration Policy", "fa fa-fw fa-puzzle-piece", IntegrationPolicyPage.class));
		
		add(new Tabbable("tabs", tabs));
		
		ConfirmDeleteRepoModal confirmDeleteDlg = new ConfirmDeleteRepoModal("confirmDeleteDlg") {

			@Override
			protected void onDeleted(AjaxRequestTarget target) {
				setResponsePage(AccountReposPage.class, AccountPage.paramsOf(getAccount()));
			}
			
		};
		add(confirmDeleteDlg);
		add(new WebMarkupContainer("delete").add(new ConfirmDeleteRepoModalBehavior(confirmDeleteDlg) {

			@Override
			protected Repository getRepository() {
				return RepoSettingPage.this.getRepository();
			}
			
		}));
	}

}
