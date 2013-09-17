package com.pmease.commons.editable;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.loader.AppLoader;
import com.pmease.commons.util.BeanUtils;

@SuppressWarnings("serial")
public abstract class AbstractReflectionBeanEditContext<T> extends BeanEditContext<T> {

	private List<PropertyEditContext<T>> propertyContexts = new ArrayList<PropertyEditContext<T>>();
	
	@SuppressWarnings("unchecked")
	public AbstractReflectionBeanEditContext(Serializable bean) {
		super(bean);

		List<Method> propertyGetters = BeanUtils.findGetters(getBean().getClass());
		EditableUtils.sortAnnotatedElements(propertyGetters);
		
		EditSupportRegistry registry = AppLoader.getInstance(EditSupportRegistry.class);
		for (Method propertyGetter: propertyGetters) {
			if (propertyGetter.getAnnotation(Editable.class) != null && BeanUtils.getSetter(propertyGetter) != null) {
				propertyContexts.add(registry.getPropertyEditContext(bean, BeanUtils.getPropertyName(propertyGetter)));
			}
		}
	}

	public List<PropertyEditContext<T>> getPropertyContexts() {
		return propertyContexts;
	}

	@Override
	public Map<Serializable, EditContext<T>> getChildContexts() {
		Map<Serializable, EditContext<T>> childContexts = new LinkedHashMap<Serializable, EditContext<T>>();
		for (PropertyEditContext<T> each: propertyContexts) {
			childContexts.put(each.getPropertyName(), each);
		}
		return childContexts;
	}

}
