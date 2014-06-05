package com.pmease.commons.wicket.editor;

import java.io.Serializable;

import com.google.inject.ImplementedBy;

@ImplementedBy(DefaultEditSupportRegistry.class)
public interface EditSupportRegistry {

	BeanEditContext<Serializable> getBeanEditContext(Class<? extends Serializable> beanClass);

	PropertyEditContext<Serializable> getPropertyEditContext(Class<? extends Serializable> beanClass, String propertyName);

}
