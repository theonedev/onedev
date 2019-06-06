package io.onedev.server.web.editable;

import java.io.Serializable;

import io.onedev.commons.launcher.loader.ExtensionPoint;

@ExtensionPoint
public interface EditSupport extends Serializable {
	
	int DEFAULT_PRIORITY = 100;
	
	PropertyContext<?> getEditContext(PropertyDescriptor descriptor);
	
	int getPriority();
	
}
