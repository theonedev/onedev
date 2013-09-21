package com.pmease.commons.editable;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.List;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.util.BeanUtils;
import com.pmease.commons.util.ClassUtils;

public abstract class AbstractListEditSupport implements EditSupport {
	
	@Override
	public BeanEditContext getBeanEditContext(Serializable bean) {
		return null;
	}

	@Override
	public PropertyEditContext getPropertyEditContext(Serializable bean, String propertyName) {
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

	@Override
	public int getPriorty() {
		return 1;
	}

	protected abstract AbstractPolymorphicListPropertyEditContext newPolymorphicListEditContext(Serializable bean, String propertyName);

	protected abstract AbstractTableListPropertyEditContext newTableListEditContext(Serializable bean, String propertyName);
}
