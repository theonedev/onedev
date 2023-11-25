package io.onedev.server.pack;

import io.onedev.server.model.Pack;
import io.onedev.server.model.Project;
import org.apache.wicket.Component;

public interface PackSupport {
	
	int getOrder();
	
	String getPackType();
	
	String getPackIcon();
	
	Component renderContent(String componentId, Pack pack);
	
	Component renderHelp(String componentId, Project project);
	
}
