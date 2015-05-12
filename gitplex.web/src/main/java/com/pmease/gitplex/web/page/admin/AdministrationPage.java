package com.pmease.gitplex.web.page.admin;

import java.util.ArrayList;
import java.util.List;

import org.apache.shiro.SecurityUtils;
import org.apache.wicket.model.Model;

import com.pmease.commons.wicket.component.tabbable.PageTab;
import com.pmease.gitplex.core.permission.Permission;
import com.pmease.gitplex.web.page.layout.SidebarPage;

@SuppressWarnings("serial")
public abstract class AdministrationPage extends SidebarPage {

	@Override
	protected boolean isPermitted() {
		return SecurityUtils.getSubject().isPermitted(Permission.ofSystemAdmin());
	}
	
	@Override
	protected List<PageTab> newSideTabs() {
		List<PageTab> tabs = new ArrayList<>();
		tabs.add(new AdministrationTab(Model.of("System Setting"), "fa fa-gear", SystemSettingPage.class));
		tabs.add(new AdministrationTab(Model.of("Mail Setting"), "fa fa-envelope", MailSettingPage.class));
		tabs.add(new AdministrationTab(Model.of("QoS Setting"), "fa fa-signal", QosSettingPage.class));
		return tabs;
	}

}
