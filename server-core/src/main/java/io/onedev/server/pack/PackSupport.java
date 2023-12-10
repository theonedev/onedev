package io.onedev.server.pack;

import io.onedev.server.model.Pack;
import io.onedev.server.model.Project;
import org.apache.wicket.Component;

import java.io.Serializable;

public interface PackSupport extends Serializable {
	
	int getOrder();
	
	String getPackType();
	
	String getPackIcon();
	
	String getProjectSeparator();
	
	String getReference(Pack pack);
	
	Component renderContent(String componentId, Pack pack);
	
	Component renderHelp(String componentId, Project project);
	
}
