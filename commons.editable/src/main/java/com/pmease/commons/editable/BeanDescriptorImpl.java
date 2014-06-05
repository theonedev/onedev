package com.pmease.commons.editable;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.util.BeanUtils;

@SuppressWarnings("serial")
public class BeanDescriptorImpl<T extends Serializable> implements BeanDescriptor<T> {

	private final Class<? extends T> beanClass;
	
	private final List<PropertyDescriptor> propertyDescriptors;
	
	public BeanDescriptorImpl(Class<? extends T> beanClass) {
		this.beanClass = beanClass;
		
		propertyDescriptors = new ArrayList<>();

		List<Method> propertyGetters = BeanUtils.findGetters(getBeanClass());
		EditableUtils.sortAnnotatedElements(propertyGetters);
		
		for (Method propertyGetter: propertyGetters) {
			if (propertyGetter.getAnnotation(Editable.class) == null)
				continue;
			Method propertySetter = BeanUtils.findSetter(propertyGetter);
			if (propertySetter == null)
				continue;
			propertyDescriptors.add(new PropertyDescriptorImpl(propertyGetter, propertySetter));
		}
	}
	
	public BeanDescriptorImpl(BeanDescriptor<T> beanDescriptor) {
		this.beanClass = beanDescriptor.getBeanClass();
		this.propertyDescriptors = beanDescriptor.getPropertyDescriptors();
	}
	
	@Override
	public Class<? extends T> getBeanClass() {
		return beanClass;
	}

	@Override
	public List<PropertyDescriptor> getPropertyDescriptors() {
		return propertyDescriptors;
	}

	@Override
	public void copyProperties(Serializable from, Serializable to) {
		for (PropertyDescriptor propertyDescriptor: getPropertyDescriptors())
			propertyDescriptor.copyProperty(from, to);
	}

	@Override
	public T newBeanInstance() {
		try {
			return getBeanClass().newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

}
