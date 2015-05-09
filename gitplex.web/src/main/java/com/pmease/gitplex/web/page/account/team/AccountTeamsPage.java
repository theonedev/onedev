package com.pmease.gitplex.web.page.account.team;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.gitplex.web.page.account.AccountPage;

@SuppressWarnings("serial")
public class AccountTeamsPage extends AccountPage {

	public AccountTeamsPage(PageParameters params) {
		super(params);
	}

	@Override
	protected String getPageTitle() {
		return "Teams - " + getAccount();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
	}
	
}
