package com.gitplex.server.web.editable;

import java.io.Serializable;
import java.util.Set;

import com.gitplex.launcher.loader.ExtensionPoint;

@ExtensionPoint
public interface EditSupport extends Serializable {
	
	BeanContext<?> getBeanEditContext(Class<?> beanClass, Set<String> excludeProperties);
	
	PropertyContext<?> getPropertyEditContext(Class<?> beanClass, String propertyName);
}
