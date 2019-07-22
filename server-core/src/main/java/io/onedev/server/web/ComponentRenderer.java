package io.onedev.server.web;

import java.io.Serializable;

import org.apache.wicket.Component;

public interface ComponentRenderer extends Serializable {
	
	Component render(String componentId);
	
}
