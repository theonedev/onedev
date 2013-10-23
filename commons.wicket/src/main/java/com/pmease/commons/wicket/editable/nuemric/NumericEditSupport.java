package com.pmease.commons.wicket.editable.nuemric;

import java.io.Serializable;
import java.lang.reflect.Method;

import com.pmease.commons.editable.BeanEditContext;
import com.pmease.commons.editable.EditSupport;
import com.pmease.commons.editable.PropertyEditContext;
import com.pmease.commons.util.BeanUtils;
import com.pmease.commons.util.JavassistUtils;

public class NumericEditSupport implements EditSupport {

	@Override
	public BeanEditContext getBeanEditContext(Serializable bean) {
		return null;
	}

	@Override
	public PropertyEditContext getPropertyEditContext(Serializable bean, String propertyName) {
		Method propertyGetter = BeanUtils.getGetter(JavassistUtils.unproxy(bean.getClass()), propertyName);
		Class<?> propertyClass = propertyGetter.getReturnType();
		if (propertyClass == int.class || propertyClass == long.class 
				|| propertyClass == Integer.class || propertyClass == Long.class) {
			return new NumericPropertyEditContext(bean, propertyName);
		} else {
			return null;
		}
	}

	@Override
	public int getPriorty() {
		return 1;
	}

}
