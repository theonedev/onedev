package com.pmease.commons.wicket.editable.string;

import java.io.Serializable;
import java.lang.reflect.Method;

import com.pmease.commons.editable.BeanEditContext;
import com.pmease.commons.editable.EditSupport;
import com.pmease.commons.editable.PropertyEditContext;
import com.pmease.commons.util.BeanUtils;
import com.pmease.commons.wicket.editable.RenderContext;

public class StringEditSupport implements EditSupport<RenderContext> {

	@Override
	public BeanEditContext<RenderContext> getBeanEditContext(Serializable bean) {
		return null;
	}

	@Override
	public PropertyEditContext<RenderContext> getPropertyEditContext(Serializable bean, String propertyName) {
		Method propertyGetter = BeanUtils.getGetter(bean.getClass(), propertyName);
		if (propertyGetter.getReturnType() == String.class) {
			return new StringPropertyEditContext(bean, propertyName);
		} else {
			return null;
		}
	}

}
