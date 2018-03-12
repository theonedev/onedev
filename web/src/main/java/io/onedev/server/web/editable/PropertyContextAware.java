package io.onedev.server.web.editable;

public interface PropertyContextAware {
	
	PropertyContext<?> getPropertyContext(String propertyName);
	
}
