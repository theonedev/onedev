package com.pmease.gitop.web.page.account.setting.password;

import com.pmease.gitop.web.page.account.setting.AccountSettingPage;

@SuppressWarnings("serial")
public class AccountPasswordPage extends AccountSettingPage {

	@Override
	protected String getPageTitle() {
		return "Change Password";
	}

	@Override
	protected Category getSettingCategory() {
		return Category.PASSWORD;
	}

}
