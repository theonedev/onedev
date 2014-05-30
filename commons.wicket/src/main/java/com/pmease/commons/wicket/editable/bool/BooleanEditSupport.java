package com.pmease.commons.wicket.editable.bool;

import java.io.Serializable;
import java.lang.reflect.Method;

import com.pmease.commons.util.BeanUtils;
import com.pmease.commons.util.JavassistUtils;
import com.pmease.commons.wicket.editable.BeanEditContext;
import com.pmease.commons.wicket.editable.EditSupport;
import com.pmease.commons.wicket.editable.PropertyEditContext;

public class BooleanEditSupport implements EditSupport {

	@Override
	public BeanEditContext getBeanEditContext(Serializable bean) {
		return null;
	}

	@Override
	public PropertyEditContext getPropertyEditContext(Serializable bean, String propertyName) {
		Method propertyGetter = BeanUtils.getGetter(JavassistUtils.unproxy(bean.getClass()), propertyName);
		Class<?> propertyClass = propertyGetter.getReturnType();
		if (propertyClass == boolean.class) {
			return new BooleanPropertyEditContext(bean, propertyName);
		} else if (propertyClass == Boolean.class) {
			return new NullableBooleanPropertyEditContext(bean, propertyName);
		} else {
			return null;
		}
	}

}