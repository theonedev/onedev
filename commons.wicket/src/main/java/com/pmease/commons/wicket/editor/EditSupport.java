package com.pmease.commons.wicket.editor;

public interface EditSupport {
	
	BeanEditContext<Object> getBeanEditContext(Class<?> beanClass);
	
	PropertyEditContext<Object> getPropertyEditContext(Class<?> beanClass, String propertyName);
}
