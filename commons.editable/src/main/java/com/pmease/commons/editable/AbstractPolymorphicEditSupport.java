package com.pmease.commons.editable;

import java.io.Serializable;
import java.lang.reflect.Method;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.util.BeanUtils;
import com.pmease.commons.util.ClassUtils;

public abstract class AbstractPolymorphicEditSupport<T> implements EditSupport<T> {

	@Override
	public BeanEditContext<T> getBeanEditContext(Serializable bean) {
		return null;
	}

	@Override
	public PropertyEditContext<T> getPropertyEditContext(Serializable bean, String propertyName) {
		Method propertyGetter = BeanUtils.getGetter(bean.getClass(), propertyName);
		Class<?> propertyClass = propertyGetter.getReturnType();
		if (propertyClass.getAnnotation(Editable.class) != null 
				&& !ClassUtils.isConcrete(propertyClass)) {
			return newPolymorphicPropertyEditContext(bean, propertyName);
		} else {
			return null;
		}
	}

	protected abstract AbstractPolymorphicPropertyEditContext<T> newPolymorphicPropertyEditContext(Serializable bean, String propertyName);

}
