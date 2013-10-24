package com.pmease.gitop.web.page.account.setting.teams;

import com.pmease.gitop.web.model.UserModel;
import com.pmease.gitop.web.page.account.setting.AccountSettingPage;

public class AccountTeamsPage extends AccountSettingPage {

	private static final long serialVersionUID = 1L;

	@Override
	protected Category getSettingCategory() {
		return Category.TEAMS;
	}

	@Override
	protected String getPageTitle() {
		return "Teams - " + getAccount();
	}

	@Override
	protected void onPageInitialize() {
		super.onPageInitialize();
		
		add(new TeamsPanel("teams", new UserModel(getAccount())));
	}
}
