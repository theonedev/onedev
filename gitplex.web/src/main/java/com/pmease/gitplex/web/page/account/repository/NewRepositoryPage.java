package com.pmease.gitplex.web.page.account.repository;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.UserManager;
import com.pmease.gitplex.web.page.account.AccountPage;

@SuppressWarnings("serial")
public class NewRepositoryPage extends AccountPage {

	public NewRepositoryPage(PageParameters params) {
		super(params);
	}
	
	@Override
	protected String getPageTitle() {
		return "Create a new repository";
	}

	@Override
	public boolean isPermitted() {
		return GitPlex.getInstance(UserManager.class).getCurrent() != null;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
	}
}
