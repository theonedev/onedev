package com.pmease.gitplex.web.page.account;

import org.apache.wicket.request.mapper.parameter.PageParameters;

@SuppressWarnings("serial")
public class MemberSettingPage extends AccountPage {

	public MemberSettingPage(PageParameters params) {
		super(params);
	}

	@Override
	protected String getPageTitle() {
		return "Members - " + getAccount();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
	}
	
}
