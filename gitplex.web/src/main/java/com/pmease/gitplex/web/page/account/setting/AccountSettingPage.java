package com.pmease.gitplex.web.page.account.setting;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.commons.wicket.component.tabbable.PageTab;
import com.pmease.commons.wicket.component.tabbable.Tabbable;
import com.pmease.gitplex.core.security.SecurityUtils;
import com.pmease.gitplex.web.page.account.AccountLayoutPage;
import com.pmease.gitplex.web.page.account.AccountTab;

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
		tabs.add(new AccountTab("Profile", "", 2, ProfileEditPage.class));
		tabs.add(new AccountTab("Avatar", "", 0, AvatarEditPage.class));
		
		if (!getAccount().isOrganization())
			tabs.add(new AccountTab("Password", "", 0, PasswordEditPage.class));
		
		add(new Tabbable("accountSettingTabs", tabs));
	}

}
