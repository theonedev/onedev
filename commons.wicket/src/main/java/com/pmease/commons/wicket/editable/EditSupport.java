package com.pmease.commons.wicket.editable;

import java.io.Serializable;

import com.pmease.commons.loader.ExtensionPoint;

@ExtensionPoint
public interface EditSupport extends Serializable {
	
	BeanContext<?> getBeanEditContext(Class<?> beanClass);
	
	PropertyContext<?> getPropertyEditContext(Class<?> beanClass, String propertyName);
}
