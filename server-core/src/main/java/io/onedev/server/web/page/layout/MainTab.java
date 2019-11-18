package io.onedev.server.web.page.layout;

import java.io.Serializable;

import org.apache.wicket.Component;

public interface MainTab extends Serializable {
	
	Component render(String componentId);
	
	boolean isAuthorized();
	
	boolean isActive(LayoutPage page);
	
}