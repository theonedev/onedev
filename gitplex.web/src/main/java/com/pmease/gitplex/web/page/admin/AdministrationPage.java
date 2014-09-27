package com.pmease.gitplex.web.page.admin;

import java.util.ArrayList;
import java.util.List;

import org.apache.shiro.SecurityUtils;
import org.apache.wicket.model.Model;

import com.pmease.commons.wicket.component.tabbable.Tab;
import com.pmease.commons.wicket.component.tabbable.Tabbable;
import com.pmease.gitplex.core.permission.ObjectPermission;
import com.pmease.gitplex.web.page.LayoutPage;

@SuppressWarnings("serial")
public abstract class AdministrationPage extends LayoutPage {

	@Override
	protected boolean isPermitted() {
		return SecurityUtils.getSubject().isPermitted(ObjectPermission.ofSystemAdmin());
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		List<Tab> tabs = new ArrayList<>();
		tabs.add(new AdministrationTab(Model.of("System Setting"), "fa fa-gear", SystemSettingPage.class));
		tabs.add(new AdministrationTab(Model.of("Mail Setting"), "fa fa-mail-alt", MailSettingPage.class));
		add(new Tabbable("tabs", tabs));
	}
	
}
