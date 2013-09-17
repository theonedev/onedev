package com.pmease.commons.editable;

import java.io.Serializable;

import com.google.inject.ImplementedBy;

@ImplementedBy(DefaultEditSupportRegistry.class)
@SuppressWarnings("rawtypes")
public interface EditSupportRegistry {

	BeanEditContext getBeanEditContext(Serializable bean);

	PropertyEditContext getPropertyEditContext(Serializable bean, String propertyName);

}
