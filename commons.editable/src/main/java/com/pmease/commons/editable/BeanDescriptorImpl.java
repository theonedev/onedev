package com.pmease.commons.editable;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.util.BeanUtils;

@SuppressWarnings("serial")
public class BeanDescriptorImpl implements BeanDescriptor {

	private final Class<?> beanClass;
	
	private final List<PropertyDescriptor> propertyDescriptors;
	
	public BeanDescriptorImpl(Class<?> beanClass) {
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
	
	public BeanDescriptorImpl(BeanDescriptor beanDescriptor) {
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
