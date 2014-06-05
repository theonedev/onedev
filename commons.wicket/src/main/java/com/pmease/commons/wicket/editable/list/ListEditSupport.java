package com.pmease.commons.wicket.editable.list;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.List;

import com.pmease.commons.editable.EditableUtils;
import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.util.BeanUtils;
import com.pmease.commons.util.ClassUtils;
import com.pmease.commons.util.JavassistUtils;
import com.pmease.commons.wicket.editable.BeanEditContext;
import com.pmease.commons.wicket.editable.EditSupport;
import com.pmease.commons.wicket.editable.PropertyEditContext;
import com.pmease.commons.wicket.editable.list.polymorphic.PolymorphicListPropertyEditConext;
import com.pmease.commons.wicket.editable.list.table.TableListPropertyEditContext;

public class ListEditSupport implements EditSupport {

	@Override
	public BeanEditContext getBeanEditContext(Serializable bean) {
		return null;
	}

	@Override
	public PropertyEditContext getPropertyEditContext(Serializable bean, String propertyName) {
		Method propertyGetter = BeanUtils.getGetter(JavassistUtils.unproxy(bean.getClass()), propertyName);
		if (propertyGetter.getReturnType() == List.class) {
			Class<?> elementClass = EditableUtils.getElementClass(propertyGetter.getGenericReturnType());
			if (elementClass != null) {
				if (ClassUtils.isConcrete(elementClass)) {
					if (elementClass.getAnnotation(Editable.class) != null)
						return new TableListPropertyEditContext(bean, propertyName);
				} else {
					if (elementClass.getAnnotation(Editable.class) != null)
						return new PolymorphicListPropertyEditConext(bean, propertyName);
				}
			}
		}
		return null;
	}

}
