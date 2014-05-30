package com.pmease.commons.wicket.editable.password;

import java.io.Serializable;
import java.lang.reflect.Method;

import com.pmease.commons.editable.Password;
import com.pmease.commons.util.BeanUtils;
import com.pmease.commons.util.JavassistUtils;
import com.pmease.commons.wicket.editable.BeanEditContext;
import com.pmease.commons.wicket.editable.EditSupport;
import com.pmease.commons.wicket.editable.PropertyEditContext;

public class PasswordEditSupport implements EditSupport {

	@Override
	public BeanEditContext getBeanEditContext(Serializable bean) {
		return null;
	}

	@Override
	public PropertyEditContext getPropertyEditContext(Serializable bean, String propertyName) {
		Method propertyGetter = BeanUtils.getGetter(JavassistUtils.unproxy(bean.getClass()), propertyName);
		if (propertyGetter.getReturnType() == String.class) {
			Password password = propertyGetter.getAnnotation(Password.class);
			if (password != null) {
				if (password.confirmative())
					return new ConfirmativePasswordPropertyEditContext(bean, propertyName);
				else
					return new PasswordPropertyEditContext(bean, propertyName);
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

}
