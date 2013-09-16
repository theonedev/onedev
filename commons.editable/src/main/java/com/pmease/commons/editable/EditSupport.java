package com.pmease.commons.editable;

import java.io.Serializable;

import com.pmease.commons.loader.ExtensionPoint;

@ExtensionPoint
public interface EditSupport {

	BeanEditContext getBeanEditContext(Serializable bean);

	PropertyEditContext getPropertyEditContext(Serializable bean, String propertyName);

}
