package io.onedev.server;

import org.apache.wicket.Component;

public interface FeatureManager {
	
	boolean isEEAvailable();
	
	boolean isEEActivated();
	
	Component renderSupportRequestLink(String componentId);
	
}
