package com.pmease.commons.wicket.editor;

import com.google.inject.ImplementedBy;

@ImplementedBy(DefaultEditSupportRegistry.class)
public interface EditSupportRegistry {

	BeanEditContext<Object> getBeanEditContext(Class<?> beanClass);

	PropertyEditContext<Object> getPropertyEditContext(Class<?> beanClass, String propertyName);

}
