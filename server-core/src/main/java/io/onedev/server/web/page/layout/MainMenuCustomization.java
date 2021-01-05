package io.onedev.server.web.page.layout;

import java.io.Serializable;
import java.util.List;

import io.onedev.server.web.page.base.BasePage;

public interface MainMenuCustomization extends Serializable {
	
	Class<? extends BasePage> getHomePage();
	
	List<SidebarMenuItem> getMainMenuItems();
	
}
