package io.onedev.server.web.editable;

import java.io.Serializable;

import io.onedev.launcher.loader.ExtensionPoint;

@ExtensionPoint
public interface EditSupport extends Serializable {
	
	PropertyContext<?> getEditContext(PropertyDescriptor descriptor);
	
}
