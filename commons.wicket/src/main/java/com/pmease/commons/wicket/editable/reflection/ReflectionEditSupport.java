package com.pmease.commons.wicket.editable.reflection;

import java.io.Serializable;
import java.lang.reflect.Method;

import com.pmease.commons.editable.Editable;
import com.pmease.commons.util.BeanUtils;
import com.pmease.commons.util.ClassUtils;
import com.pmease.commons.util.JavassistUtils;
import com.pmease.commons.wicket.editable.BeanEditContext;
import com.pmease.commons.wicket.editable.EditSupport;
import com.pmease.commons.wicket.editable.PropertyEditContext;

public class ReflectionEditSupport implements EditSupport  {

	@Override
	public BeanEditContext getBeanEditContext(Serializable bean) {
	    Class<?> beanClass = JavassistUtils.unproxy(bean.getClass());
		if (beanClass.getAnnotation(Editable.class) != null && ClassUtils.isConcrete(beanClass))
			return new ReflectionBeanEditContext(bean);
		else
			return null;
	}

	@Override
	public PropertyEditContext getPropertyEditContext(Serializable bean, String propertyName) {
		Method propertyGetter = BeanUtils.getGetter(JavassistUtils.unproxy(bean.getClass()), propertyName);
		Class<?> propertyClass = propertyGetter.getReturnType();
		if (propertyClass.getAnnotation(Editable.class) != null && ClassUtils.isConcrete(propertyClass)) {
			return new ReflectionPropertyEditContext(bean, propertyName);
		} else {
			return null;
		}
	}

}
