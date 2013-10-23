package com.pmease.commons.editable;

import java.io.Serializable;
import java.lang.reflect.Method;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.util.BeanUtils;
import com.pmease.commons.util.ClassUtils;
import com.pmease.commons.util.JavassistUtils;

public abstract class AbstractReflectionEditSupport implements EditSupport {
	
	@Override
	public BeanEditContext getBeanEditContext(Serializable bean) {
	    Class<?> beanClass = JavassistUtils.unproxy(bean.getClass());
		if (beanClass.getAnnotation(Editable.class) != null && ClassUtils.isConcrete(beanClass))
			return newReflectionBeanEditContext(bean);
		else
			return null;
	}

	@Override
	public PropertyEditContext getPropertyEditContext(Serializable bean, String propertyName) {
		Method propertyGetter = BeanUtils.getGetter(JavassistUtils.unproxy(bean.getClass()), propertyName);
		Class<?> propertyClass = propertyGetter.getReturnType();
		if (propertyClass.getAnnotation(Editable.class) != null && ClassUtils.isConcrete(propertyClass)) {
			return newReflectionPropertyEditContext(bean, propertyName);
		} else {
			return null;
		}
	}

	@Override
	public int getPriorty() {
		return 1;
	}

	protected abstract AbstractReflectionBeanEditContext newReflectionBeanEditContext(Serializable bean);
	
	protected abstract AbstractReflectionPropertyEditContext newReflectionPropertyEditContext(Serializable bean, String propertyName);

}
