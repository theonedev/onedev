package com.pmease.gitplex.web.page.depot.setting;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;

import com.pmease.commons.wicket.component.tabbable.PageTab;
import com.pmease.commons.wicket.component.tabbable.Tabbable;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.security.SecurityUtils;
import com.pmease.gitplex.web.page.depot.DepotPage;
import com.pmease.gitplex.web.page.depot.setting.gatekeeper.GateKeeperPage;
import com.pmease.gitplex.web.page.depot.setting.general.GeneralSettingPage;
import com.pmease.gitplex.web.page.depot.setting.integrationpolicy.IntegrationPolicyPage;

@SuppressWarnings("serial")
public class DepotSettingPage extends DepotPage {

	public DepotSettingPage(PageParameters params) {
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
		tabs.add(new DepotSettingTab("General Setting", GeneralSettingPage.class));
		tabs.add(new DepotSettingTab("Gate Keepers", GateKeeperPage.class));
		tabs.add(new DepotSettingTab("Integration Policies", IntegrationPolicyPage.class));
		
		add(new Tabbable("repoSettingTabs", tabs));
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(CssHeaderItem.forReference(new CssResourceReference(DepotSettingPage.class, "repo-setting.css")));
	}

	@Override
	protected void onSelect(AjaxRequestTarget target, Depot depot) {
		setResponsePage(DepotSettingPage.class, paramsOf(depot));
	}
	
}
