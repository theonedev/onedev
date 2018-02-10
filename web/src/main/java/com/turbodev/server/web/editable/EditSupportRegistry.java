package com.turbodev.server.web.editable;

import java.io.Serializable;
import java.util.Set;

public interface EditSupportRegistry {

	BeanContext<Serializable> getBeanEditContext(Class<?> beanClass, Set<String> excludeProperties);

	PropertyContext<Serializable> getPropertyEditContext(Class<?> beanClass, String propertyName);

}
