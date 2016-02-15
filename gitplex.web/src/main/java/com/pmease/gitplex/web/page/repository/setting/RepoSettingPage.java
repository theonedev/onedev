package com.pmease.gitplex.web.page.repository.setting;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;

import com.pmease.commons.wicket.component.tabbable.PageTab;
import com.pmease.commons.wicket.component.tabbable.Tabbable;
import com.pmease.gitplex.core.model.Depot;
import com.pmease.gitplex.core.security.SecurityUtils;
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
		return SecurityUtils.canManage(getDepot());
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		List<PageTab> tabs = new ArrayList<>();
		tabs.add(new RepoSettingTab("General Setting", GeneralSettingPage.class));
		tabs.add(new RepoSettingTab("Gate Keepers", GateKeeperPage.class));
		tabs.add(new RepoSettingTab("Integration Policies", IntegrationPolicyPage.class));
		
		add(new Tabbable("repoSettingTabs", tabs));
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(CssHeaderItem.forReference(new CssResourceReference(RepoSettingPage.class, "repo-setting.css")));
	}

	@Override
	protected void onSelect(AjaxRequestTarget target, Depot depot) {
		setResponsePage(RepoSettingPage.class, paramsOf(depot));
	}
	
}
