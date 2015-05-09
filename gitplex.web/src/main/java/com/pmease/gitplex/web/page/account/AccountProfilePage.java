package com.pmease.gitplex.web.page.account;

import org.apache.wicket.request.mapper.parameter.PageParameters;

@SuppressWarnings("serial")
public class AccountProfilePage extends AccountPage {

	public AccountProfilePage(PageParameters params) {
		super(params);
	}
	
	@Override
	protected String getPageTitle() {
		return "Profile - " + getAccount();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
	}

}
