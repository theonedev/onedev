package io.onedev.server.web.page.layout;

import java.io.Serializable;
import java.util.List;

import org.apache.wicket.core.request.handler.PageProvider;

public interface MainMenuCustomization extends Serializable {
	
	PageProvider getHomePage(boolean failsafe);
	
	List<SidebarMenuItem> getMainMenuItems();
	
}
