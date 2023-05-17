package io.onedev.server.ee;

import io.onedev.server.ee.dashboard.DashboardPage;
import io.onedev.server.ee.xsearch.FileSearchPage;
import io.onedev.server.ee.xsearch.SymbolSearchPage;
import io.onedev.server.ee.xsearch.TextSearchPage;
import io.onedev.server.web.page.layout.DefaultMainMenuCustomization;
import io.onedev.server.web.page.layout.SidebarMenuItem;
import org.apache.wicket.core.request.handler.PageProvider;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

@Singleton 
public class EEMainMenuCustomization extends DefaultMainMenuCustomization {

	private static final long serialVersionUID = 1L;

	@Override
	public PageProvider getHomePage(boolean failsafe) {
		return new PageProvider(DashboardPage.class, DashboardPage.paramsOf(null, failsafe));
	}

	@Override
	public List<SidebarMenuItem> getMainMenuItems() {
		var menuItems = new ArrayList<SidebarMenuItem>();
		
		menuItems.add(new SidebarMenuItem.Page("dashboard", "Dashboards", DashboardPage.class, 
				new PageParameters()));
		menuItems.addAll(super.getMainMenuItems());
		
		var codeSearchMenuItems = new ArrayList<SidebarMenuItem>();
		codeSearchMenuItems.add(new SidebarMenuItem.Page(null, "Text", TextSearchPage.class, new PageParameters()));
		codeSearchMenuItems.add(new SidebarMenuItem.Page(null, "Files", FileSearchPage.class, new PageParameters()));
		codeSearchMenuItems.add(new SidebarMenuItem.Page(null, "Symbols", SymbolSearchPage.class, new PageParameters()));
		menuItems.add(new SidebarMenuItem.SubMenu("code", "Code Search", codeSearchMenuItems));
		
		return menuItems;
	}

}
