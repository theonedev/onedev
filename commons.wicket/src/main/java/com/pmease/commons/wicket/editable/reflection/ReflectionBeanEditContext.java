package com.pmease.commons.wicket.editable.reflection;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Component;

import com.pmease.commons.editable.Editable;
import com.pmease.commons.loader.AppLoader;
import com.pmease.commons.util.BeanUtils;
import com.pmease.commons.wicket.editable.BeanEditContext;
import com.pmease.commons.wicket.editable.EditContext;
import com.pmease.commons.wicket.editable.EditSupportRegistry;
import com.pmease.commons.wicket.editable.EditableUtils;
import com.pmease.commons.wicket.editable.PropertyEditContext;

@SuppressWarnings("serial")
public class ReflectionBeanEditContext extends BeanEditContext {

	private List<PropertyEditContext> propertyContexts = new ArrayList<PropertyEditContext>();
	
	public ReflectionBeanEditContext(Serializable bean) {
		super(bean);

		List<Method> propertyGetters = BeanUtils.findGetters(getBeanClass());
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

	@Override
	public Component renderForEdit(String componentId) {
		return new ReflectionBeanEditor(componentId, this);
	}

	@Override
	public Component renderForView(String componentId) {
		return new ReflectionBeanViewer(componentId, this);
	}

}
