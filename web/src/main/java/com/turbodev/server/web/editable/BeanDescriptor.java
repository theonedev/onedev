package com.turbodev.server.web.editable;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.turbodev.utils.BeanUtils;
import com.google.common.collect.Sets;
import com.turbodev.server.util.editable.EditableUtils;
import com.turbodev.server.util.editable.annotation.Editable;

@SuppressWarnings("serial")
public class BeanDescriptor implements Serializable {

	private final Class<?> beanClass;
	
	protected final List<PropertyDescriptor> propertyDescriptors;
	
	public BeanDescriptor(Class<?> beanClass) {
		this(beanClass, Sets.newHashSet());
	}
	
	public BeanDescriptor(Class<?> beanClass, Set<String> excludeProperties) {
		this.beanClass = beanClass;
		
		propertyDescriptors = new ArrayList<>();

		List<Method> propertyGetters = BeanUtils.findGetters(getBeanClass());
		EditableUtils.sortAnnotatedElements(propertyGetters);
		
		for (Method propertyGetter: propertyGetters) {
			if (propertyGetter.getAnnotation(Editable.class) == null 
					|| excludeProperties.contains(BeanUtils.getPropertyName(propertyGetter))) {
				continue;
			}
			Method propertySetter = BeanUtils.findSetter(propertyGetter);
			if (propertySetter == null)
				continue;
			propertyDescriptors.add(new PropertyDescriptor(propertyGetter, propertySetter));
		}
	}
	
	public BeanDescriptor(BeanDescriptor beanDescriptor) {
		this.beanClass = beanDescriptor.getBeanClass();
		this.propertyDescriptors = beanDescriptor.getPropertyDescriptors();
	}
	
	public Class<?> getBeanClass() {
		return beanClass;
	}

	public List<PropertyDescriptor> getPropertyDescriptors() {
		return propertyDescriptors;
	}

	public void copyProperties(Object from, Object to) {
		for (PropertyDescriptor propertyDescriptor: getPropertyDescriptors())
			propertyDescriptor.copyProperty(from, to);
	}

	public Object newBeanInstance() {
		try {
			return getBeanClass().newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

}
