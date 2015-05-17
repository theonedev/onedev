package com.pmease.gitplex.web.page.admin;

import java.util.ArrayList;
import java.util.List;

import org.apache.shiro.SecurityUtils;

import com.pmease.commons.wicket.component.tabbable.PageTab;
import com.pmease.gitplex.core.permission.ObjectPermission;
import com.pmease.gitplex.web.component.sidebar.SidebarBorder;
import com.pmease.gitplex.web.page.main.MainPage;

@SuppressWarnings("serial")
public abstract class AdministrationPage extends MainPage {

	protected SidebarBorder sidebar;
	
	@Override
	protected boolean isPermitted() {
		return SecurityUtils.getSubject().isPermitted(ObjectPermission.ofSystemAdmin());
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		List<PageTab> tabs = new ArrayList<>();
		tabs.add(new AdministrationTab("System Setting", "fa fa-gear", SystemSettingPage.class));
		tabs.add(new AdministrationTab("Mail Setting", "fa fa-envelope", MailSettingPage.class));
		tabs.add(new AdministrationTab("QoS Setting", "fa fa-signal", QosSettingPage.class));
		
		add(sidebar = new SidebarBorder("sidebar", tabs));
	}

}
