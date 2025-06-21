package io.onedev.server.web.page.project;

import java.util.List;

import io.onedev.server.model.Project;
import io.onedev.server.web.page.layout.SidebarMenuItem;

public interface ProjectMenuContribution {

	List<SidebarMenuItem> getMenuItems(Project project);
	
	int getOrder();
	
}
