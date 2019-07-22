package io.onedev.server.web;

import org.apache.wicket.Component;

public interface PrioritizedComponentRenderer extends ComponentRenderer {
	
	@Override
	Component render(String componentId);
	
	int getPriority();
	
}
