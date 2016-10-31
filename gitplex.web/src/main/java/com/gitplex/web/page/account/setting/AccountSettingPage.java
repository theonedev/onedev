package com.gitplex.web.page.account.setting;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.gitplex.core.security.SecurityUtils;
import com.gitplex.web.page.account.AccountLayoutPage;
import com.gitplex.web.page.account.AccountTab;
import com.gitplex.commons.wicket.component.tabbable.PageTab;
import com.gitplex.commons.wicket.component.tabbable.Tabbable;

@SuppressWarnings("serial")
public abstract class AccountSettingPage extends AccountLayoutPage {

	public AccountSettingPage(PageParameters params) {
		super(params);
	}

	@Override
	protected boolean isPermitted() {
		return SecurityUtils.canManage(getAccount());
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		List<PageTab> tabs = new ArrayList<>();
		tabs.add(new AccountTab("Profile", "", 0, ProfileEditPage.class));
		tabs.add(new AccountTab("Avatar", "", 0, AvatarEditPage.class));
		
		if (!getAccount().isOrganization())
			tabs.add(new AccountTab("Change Password", "", 0, PasswordEditPage.class));
		
		add(new Tabbable("accountSettingTabs", tabs));
	}

}
