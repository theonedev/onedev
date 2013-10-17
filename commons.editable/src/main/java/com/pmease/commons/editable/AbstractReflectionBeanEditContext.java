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
public abstract class AbstractReflectionBeanEditContext extends BeanEditContext {

	private List<PropertyEditContext> propertyContexts = new ArrayList<PropertyEditContext>();
	
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

	public List<PropertyEditContext> getPropertyContexts() {
		return propertyContexts;
	}

	@Override
	public Map<Serializable, EditContext> getChildContexts() {
		Map<Serializable, EditContext> childContexts = new LinkedHashMap<>();
		for (PropertyEditContext each: propertyContexts) {
			childContexts.put(each.getPropertyName(), each);
		}
		return childContexts;
	}

}
