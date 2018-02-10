package com.turbodev.server.web.editable;

import java.io.Serializable;
import java.util.Set;

import com.turbodev.launcher.loader.ExtensionPoint;

@ExtensionPoint
public interface EditSupport extends Serializable {
	
	BeanContext<?> getBeanEditContext(Class<?> beanClass, Set<String> excludeProperties);
	
	PropertyContext<?> getPropertyEditContext(Class<?> beanClass, String propertyName);
}
