package com.pmease.commons.wicket.editable;

import java.io.Serializable;
import java.lang.reflect.Method;

import com.pmease.commons.editable.Editable;
import com.pmease.commons.util.BeanUtils;
import com.pmease.commons.util.ClassUtils;
import com.pmease.commons.util.JavassistUtils;

public abstract class AbstractPolymorphicEditSupport implements EditSupport {

	@Override
	public BeanEditContext getBeanEditContext(Serializable bean) {
		return null;
	}

	@Override
	public PropertyEditContext getPropertyEditContext(Serializable bean, String propertyName) {
		Method propertyGetter = BeanUtils.getGetter(JavassistUtils.unproxy(bean.getClass()), propertyName);
		Class<?> propertyClass = propertyGetter.getReturnType();
		if (propertyClass.getAnnotation(Editable.class) != null 
				&& !ClassUtils.isConcrete(propertyClass)) {
			return newPolymorphicPropertyEditContext(bean, propertyName);
		} else {
			return null;
		}
	}

	protected abstract AbstractPolymorphicPropertyEditContext newPolymorphicPropertyEditContext(Serializable bean, String propertyName);

}
