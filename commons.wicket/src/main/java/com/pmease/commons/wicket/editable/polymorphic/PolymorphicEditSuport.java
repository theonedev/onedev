package com.pmease.commons.wicket.editable.polymorphic;

import java.io.Serializable;

import com.pmease.commons.editable.AbstractPolymorphicEditSupport;
import com.pmease.commons.editable.AbstractPolymorphicPropertyEditContext;

public class PolymorphicEditSuport extends AbstractPolymorphicEditSupport {

	@Override
	protected AbstractPolymorphicPropertyEditContext newPolymorphicPropertyEditContext(
			Serializable bean, String propertyName) {
		return new PolymorphicPropertyEditContext(bean, propertyName);
	}

	@Override
	public int getPriorty() {
		return 1;
	}

}
