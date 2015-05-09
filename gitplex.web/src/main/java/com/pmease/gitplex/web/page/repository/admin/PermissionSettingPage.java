package com.pmease.gitplex.web.page.repository.admin;

import org.apache.shiro.SecurityUtils;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.gitplex.core.permission.Permission;
import com.pmease.gitplex.web.page.repository.RepositoryPage;

@SuppressWarnings("serial")
public class PermissionSettingPage extends RepositoryPage {

	public PermissionSettingPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
	}
	
	@Override
	protected String getPageTitle() {
		return "Permissions - " + getRepository();
	}

	@Override
	protected boolean isPermitted() {
		return super.isPermitted() 
				&& SecurityUtils.getSubject().isPermitted(Permission.ofRepositoryAdmin(getRepository()));
	}
}
