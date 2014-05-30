package com.pmease.commons.wicket.editable;

import java.io.Serializable;

import com.google.inject.ImplementedBy;

@ImplementedBy(DefaultEditSupportRegistry.class)
public interface EditSupportRegistry {

	BeanEditContext getBeanEditContext(Serializable bean);

	PropertyEditContext getPropertyEditContext(Serializable bean, String propertyName);

}
