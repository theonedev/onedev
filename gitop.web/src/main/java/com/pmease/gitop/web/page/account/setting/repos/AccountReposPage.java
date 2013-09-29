package com.pmease.gitop.web.page.account.setting.repos;

import com.pmease.gitop.web.page.account.setting.AccountSettingPage;

@SuppressWarnings("serial")
public class AccountReposPage extends AccountSettingPage {

	@Override
	protected String getPageTitle() {
		return "Your Repositories";
	}

	@Override
	protected Category getSettingCategory() {
		return Category.REPOS;
	}
}
