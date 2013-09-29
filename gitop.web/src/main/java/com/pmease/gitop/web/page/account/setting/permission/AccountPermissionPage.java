package com.pmease.gitop.web.page.account.setting.permission;

import com.pmease.gitop.web.page.account.setting.AccountSettingPage;

@SuppressWarnings("serial")
public class AccountPermissionPage extends AccountSettingPage {

	@Override
	protected String getPageTitle() {
		return "Account Level Permissions";
	}

	@Override
	protected Category getSettingCategory() {
		return Category.PERMISSION;
	}

}
