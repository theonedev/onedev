package com.pmease.commons.wicket.editable;

import java.io.Serializable;

public interface EditSupportRegistry {

	BeanContext<Serializable> getBeanEditContext(Class<?> beanClass);

	PropertyContext<Serializable> getPropertyEditContext(Class<?> beanClass, String propertyName);

}
