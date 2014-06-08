package com.pmease.commons.wicket.editable;

import java.io.Serializable;

import com.google.inject.ImplementedBy;

@ImplementedBy(DefaultEditSupportRegistry.class)
public interface EditSupportRegistry {

	BeanContext<Serializable> getBeanEditContext(Class<?> beanClass);

	PropertyContext<Serializable> getPropertyEditContext(Class<?> beanClass, String propertyName);

}
