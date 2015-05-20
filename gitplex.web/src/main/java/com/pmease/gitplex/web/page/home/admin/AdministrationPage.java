package com.pmease.gitplex.web.page.home.admin;

import java.util.ArrayList;
import java.util.List;

import org.apache.shiro.SecurityUtils;

import com.pmease.commons.wicket.component.tabbable.PageTab;
import com.pmease.commons.wicket.component.tabbable.Tabbable;
import com.pmease.gitplex.core.permission.ObjectPermission;
import com.pmease.gitplex.web.page.layout.LayoutPage;

@SuppressWarnings("serial")
public abstract class AdministrationPage extends LayoutPage {

	@Override
	protected boolean isPermitted() {
		return SecurityUtils.getSubject().isPermitted(ObjectPermission.ofSystemAdmin());
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		List<PageTab> tabs = new ArrayList<>();
		tabs.add(new AdministrationTab("System Setting", "fa fa-fw fa-gear", SystemSettingPage.class));
		tabs.add(new AdministrationTab("Mail Setting", "fa fa-fw fa-envelope", MailSettingPage.class));
		tabs.add(new AdministrationTab("QoS Setting", "fa fa-fw fa-signal", QosSettingPage.class));
		
		add(new Tabbable("tabs", tabs));
	}

}
