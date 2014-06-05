package com.pmease.commons.wicket.editor;

import java.io.Serializable;

import com.pmease.commons.loader.ExtensionPoint;

@ExtensionPoint
public interface EditSupport {
	
	BeanEditContext<? extends Serializable> getBeanEditContext(Class<? extends Serializable> beanClass);
	
	PropertyEditContext<? extends Serializable> getPropertyEditContext(Class<? extends Serializable> beanClass, String propertyName);
}
