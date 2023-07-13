package io.onedev.server.web.page.layout;

import java.io.Serializable;
import java.util.List;

public interface AdministrationMenuContribution extends Serializable {
	
	List<SidebarMenuItem> getAdministrationMenuItems();
	
}
