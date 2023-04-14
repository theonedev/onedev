package io.onedev.server.ee.dashboard;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.core.request.handler.PageProvider;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.web.page.layout.DefaultMainMenuCustomization;
import io.onedev.server.web.page.layout.SidebarMenuItem;

public class EEMainMenuCustomization extends DefaultMainMenuCustomization {

	private static final long serialVersionUID = 1L;

	@Override
	public PageProvider getHomePage(boolean failsafe) {
		return new PageProvider(DashboardPage.class, DashboardPage.paramsOf(null, failsafe));
	}

	@Override
	public List<SidebarMenuItem> getMainMenuItems() {
		List<SidebarMenuItem> menuItems = new ArrayList<>();
		
		menuItems.add(new SidebarMenuItem.Page("dashboard", "Dashboards", DashboardPage.class, 
				new PageParameters()));
		menuItems.addAll(super.getMainMenuItems());
		
		return menuItems;
	}

}
