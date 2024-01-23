package io.onedev.server.web.editable;

import java.io.Serializable;

import io.onedev.commons.loader.ExtensionPoint;

import javax.annotation.Nullable;

@ExtensionPoint
public interface EditSupport extends Serializable {
	
	int DEFAULT_PRIORITY = 100;
	
	@Nullable
	PropertyContext<?> getEditContext(PropertyDescriptor descriptor);
	
	int getPriority();
	
}
