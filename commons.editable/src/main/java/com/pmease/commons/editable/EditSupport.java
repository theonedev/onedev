package com.pmease.commons.editable;

import java.io.Serializable;

import com.pmease.commons.loader.ExtensionPoint;

@ExtensionPoint
public interface EditSupport<T> {

	BeanEditContext<T> getBeanEditContext(Serializable bean);

	PropertyEditContext<T> getPropertyEditContext(Serializable bean, String propertyName);

}
