package com.pmease.gitplex.web.page.repository.admin;

import java.util.List;

import org.apache.shiro.SecurityUtils;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.collect.Lists;
import com.pmease.commons.wicket.component.tabbable.StylelessTabbable;
import com.pmease.commons.wicket.component.tabbable.Tab;
import com.pmease.gitplex.core.permission.ObjectPermission;
import com.pmease.gitplex.web.page.repository.RepositoryPage;

@SuppressWarnings("serial")
public abstract class RepoAdminPage extends RepositoryPage {

	public RepoAdminPage(PageParameters params) {
		super(params);
	}
	
	@Override
	protected boolean isPermitted() {
		return SecurityUtils.getSubject().isPermitted(ObjectPermission.ofRepositoryAdmin(getRepository()));
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		List<Tab> tabs = Lists.newArrayList();
		
		tabs.add(new RepoAdminTab(Model.of("Options"), RepoOptionsPage.class));
		tabs.add(new RepoAdminTab(Model.of("Gate Keepers"), GateKeeperSettingPage.class));
		tabs.add(new RepoAdminTab(Model.of("Integration Setting"), IntegrationSettingPage.class));
		tabs.add(new RepoAdminTab(Model.of("Hooks"), RepoHooksPage.class));
		tabs.add(new RepoAdminTab(Model.of("Audit Log"), RepoAuditPage.class));
		tabs.add(new RepoAdminTab(Model.of("Permissions"), RepoPermissionsPage.class));

		add(new StylelessTabbable("tabs", tabs));
	}
	
}
