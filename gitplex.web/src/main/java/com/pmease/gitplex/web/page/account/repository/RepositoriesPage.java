package com.pmease.gitplex.web.page.account.repository;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.gitplex.web.page.account.AccountPage;

@SuppressWarnings("serial")
public class RepositoriesPage extends AccountPage {

	public RepositoriesPage(PageParameters params) {
		super(params);
	}

	@Override
	protected String getPageTitle() {
		return "Repositories - " + getAccount();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
	}
}
