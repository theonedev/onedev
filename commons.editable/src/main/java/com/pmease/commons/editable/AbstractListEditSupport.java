package com.pmease.commons.editable;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.List;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.util.BeanUtils;
import com.pmease.commons.util.ClassUtils;

public abstract class AbstractListEditSupport<T> implements EditSupport<T> {
	
	@Override
	public BeanEditContext<T> getBeanEditContext(Serializable bean) {
		return null;
	}

	@Override
	public PropertyEditContext<T> getPropertyEditContext(Serializable bean, String propertyName) {
		Method propertyGetter = BeanUtils.getGetter(bean.getClass(), propertyName);
		if (propertyGetter.getReturnType() == List.class) {
			Class<?> elementClass = EditableUtils.getElementClass(propertyGetter.getGenericReturnType());
			if (elementClass != null) {
				if (ClassUtils.isConcrete(elementClass)) {
					if (elementClass.getAnnotation(Editable.class) != null)
						return newTableListEditContext(bean, propertyName);
				} else {
					if (elementClass.getAnnotation(Editable.class) != null)
						return newPolymorphicListEditContext(bean, propertyName);
				}
			}
		}
		return null;
	}

	protected abstract AbstractPolymorphicListPropertyEditContext<T> newPolymorphicListEditContext(Serializable bean, String propertyName);

	protected abstract AbstractTableListPropertyEditContext<T> newTableListEditContext(Serializable bean, String propertyName);
}
