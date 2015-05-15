package com.pmease.gitplex.web.page.account.setting;

import java.util.ArrayList;
import java.util.List;

import org.apache.shiro.SecurityUtils;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.commons.wicket.component.tabbable.PageTab;
import com.pmease.gitplex.core.permission.Permission;
import com.pmease.gitplex.web.component.sidebar.SidebarBorder;
import com.pmease.gitplex.web.page.account.AccountPage;

@SuppressWarnings("serial")
public class AccountSettingPage extends AccountPage {

	public AccountSettingPage(PageParameters params) {
		super(params);
	}

	protected SidebarBorder sidebar;
	
	@Override
	protected boolean isPermitted() {
		return SecurityUtils.getSubject().isPermitted(Permission.ofUserAdmin(getAccount()));
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		List<PageTab> tabs = new ArrayList<>();
		tabs.add(new AccountSettingTab("Profile", "fa fa-pencil", ProfileEditPage.class));
		tabs.add(new AccountSettingTab("Password", "fa fa-key", PasswordEditPage.class));
		tabs.add(new AccountSettingTab("Avatar", "fa fa-user", AvatarEditPage.class));
		
		add(sidebar = new SidebarBorder("sidebar", tabs));
	}

}
