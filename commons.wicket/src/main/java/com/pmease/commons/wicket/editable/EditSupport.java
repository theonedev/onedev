package com.pmease.commons.wicket.editable;

import java.io.Serializable;
import java.util.Set;

import com.pmease.commons.loader.ExtensionPoint;

@ExtensionPoint
public interface EditSupport extends Serializable {
	
	BeanContext<?> getBeanEditContext(Class<?> beanClass, Set<String> excludeProperties);
	
	PropertyContext<?> getPropertyEditContext(Class<?> beanClass, String propertyName);
}
