package com.pmease.gitplex.web.page.admin;

import java.util.ArrayList;
import java.util.List;

import org.apache.shiro.SecurityUtils;
import org.apache.wicket.model.Model;

import com.pmease.commons.wicket.component.tabbable.PageTab;
import com.pmease.commons.wicket.component.tabbable.StylelessTabbable;
import com.pmease.commons.wicket.component.tabbable.Tab;
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

		List<Tab> tabs = new ArrayList<>();
		tabs.add(new PageTab(Model.of("System Setting"), SystemSettingPage.class));
		tabs.add(new PageTab(Model.of("Mail Setting"), MailSettingPage.class));
		add(new StylelessTabbable("tabs", tabs));
	}
	
}
