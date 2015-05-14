package com.pmease.gitplex.web.page.account.setting;

import java.util.ArrayList;
import java.util.List;

import org.apache.shiro.SecurityUtils;

import com.pmease.commons.wicket.component.tabbable.PageTab;
import com.pmease.gitplex.core.permission.Permission;
import com.pmease.gitplex.web.component.sidebar.SidebarBorder;
import com.pmease.gitplex.web.page.main.MainPage;

@SuppressWarnings("serial")
public abstract class AdministrationPage extends MainPage {

	protected SidebarBorder sidebar;
	
	@Override
	protected boolean isPermitted() {
		return SecurityUtils.getSubject().isPermitted(Permission.ofSystemAdmin());
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		List<PageTab> tabs = new ArrayList<>();
		
		add(sidebar = new SidebarBorder("sidebar", tabs));
	}

}
