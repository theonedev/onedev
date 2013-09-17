package com.pmease.commons.wicket.editable.polymorphic;

import java.io.Serializable;

import com.pmease.commons.editable.AbstractPolymorphicEditSupport;
import com.pmease.commons.editable.AbstractPolymorphicPropertyEditContext;
import com.pmease.commons.wicket.editable.RenderContext;

public class PolymorphicEditSuport extends AbstractPolymorphicEditSupport<RenderContext> {

	@Override
	protected AbstractPolymorphicPropertyEditContext<RenderContext> newPolymorphicPropertyEditContext(
			Serializable bean, String propertyName) {
		return new PolymorphicPropertyEditContext(bean, propertyName);
	}

}
