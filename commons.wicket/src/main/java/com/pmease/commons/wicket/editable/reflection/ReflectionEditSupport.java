package com.pmease.commons.wicket.editable.reflection;

import java.io.Serializable;

import com.pmease.commons.wicket.editable.AbstractReflectionEditSupport;

public class ReflectionEditSupport extends AbstractReflectionEditSupport {

	@Override
	protected ReflectionBeanEditContext newReflectionBeanEditContext(Serializable bean) {
		return new ReflectionBeanEditContext(bean);
	}

	@Override
	protected ReflectionPropertyEditContext newReflectionPropertyEditContext(Serializable bean, String propertyName) {
		return new ReflectionPropertyEditContext(bean, propertyName);
	}

}
