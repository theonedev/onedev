package com.pmease.commons.wicket.editable.list.polymorphic;

import java.io.Serializable;

import com.pmease.commons.editable.AbstractPolymorphicListPropertyEditContext;

@SuppressWarnings("serial")
public class PolymorphicListPropertyEditConext extends AbstractPolymorphicListPropertyEditContext {

	public PolymorphicListPropertyEditConext(Serializable bean, String propertyName) {
		super(bean, propertyName);
	}

}
