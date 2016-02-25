package com.pmease.commons.wicket.editable;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;
import com.pmease.commons.util.BeanUtils;
import com.pmease.commons.wicket.editable.annotation.Editable;

@SuppressWarnings("serial")
public class DefaultBeanDescriptor implements BeanDescriptor {

	private final Class<?> beanClass;
	
	protected final List<PropertyDescriptor> propertyDescriptors;
	
	public DefaultBeanDescriptor(Class<?> beanClass) {
		this(beanClass, Sets.newHashSet());
	}
	
	public DefaultBeanDescriptor(Class<?> beanClass, Set<String> excludeProperties) {
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
			propertyDescriptors.add(new DefaultPropertyDescriptor(propertyGetter, propertySetter));
		}
	}
	
	public DefaultBeanDescriptor(BeanDescriptor beanDescriptor) {
		this.beanClass = beanDescriptor.getBeanClass();
		this.propertyDescriptors = beanDescriptor.getPropertyDescriptors();
	}
	
	@Override
	public Class<?> getBeanClass() {
		return beanClass;
	}

	@Override
	public List<PropertyDescriptor> getPropertyDescriptors() {
		return propertyDescriptors;
	}

	@Override
	public void copyProperties(Object from, Object to) {
		for (PropertyDescriptor propertyDescriptor: getPropertyDescriptors())
			propertyDescriptor.copyProperty(from, to);
	}

	@Override
	public Object newBeanInstance() {
		try {
			return getBeanClass().newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

}
