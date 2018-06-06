package io.onedev.server.web.editable;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.Sets;

import io.onedev.server.exception.OneException;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.utils.BeanUtils;

@SuppressWarnings("serial")
public class BeanDescriptor implements Serializable {

	private final Class<?> beanClass;
	
	protected final List<PropertyDescriptor> propertyDescriptors;
	
	public BeanDescriptor(Class<?> beanClass) {
		this(beanClass, Sets.newHashSet());
	}
	
	public BeanDescriptor(Class<?> beanClass, Set<String> excludedProperties) {
		this.beanClass = beanClass;
		
		propertyDescriptors = new ArrayList<>();

		List<Method> propertyGetters = BeanUtils.findGetters(getBeanClass());
		EditableUtils.sortAnnotatedElements(propertyGetters);
		
		for (Method propertyGetter: propertyGetters) {
			if (propertyGetter.getAnnotation(Editable.class) != null) {
				PropertyDescriptor propertyDescriptor = new PropertyDescriptor(propertyGetter); 
				propertyDescriptors.add(propertyDescriptor);
				String propertyName = BeanUtils.getPropertyName(propertyGetter);
				propertyDescriptor.setPropertyExcluded(BeanUtils.findSetter(propertyGetter) == null || excludedProperties.contains(propertyName));
			}
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
	
	public Map<String, PropertyDescriptor> getMapOfDisplayNameToPropertyDescriptor() {
		Map<String, PropertyDescriptor> propertyDescriptors = new HashMap<>();
		for (PropertyDescriptor propertyDescriptor: getPropertyDescriptors())
			propertyDescriptors.put(EditableUtils.getDisplayName(propertyDescriptor.getPropertyGetter()), propertyDescriptor);
		return propertyDescriptors;
	}

	public void copyProperties(Object from, Object to) {
		for (PropertyDescriptor propertyDescriptor: getPropertyDescriptors()) {
			if (!propertyDescriptor.isPropertyExcluded())
				propertyDescriptor.copyProperty(from, to);
		}
	}
	
	@Nullable
	public PropertyDescriptor getPropertyDescriptor(String propertyName) {
		for (PropertyDescriptor propertyDescriptor: getPropertyDescriptors()) {
			if (propertyDescriptor.getPropertyName().equals(propertyName))
				return propertyDescriptor;
		}
		return null;
	}

	public Object newBeanInstance() {
		try {
			return getBeanClass().newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	public String getPropertyName(String propertyNameOrDisplayName) {
		for (PropertyDescriptor propertyDescriptor: propertyDescriptors) {
			String displayName = propertyDescriptor.getDisplayName();
			if (propertyDescriptor.getPropertyName().equals(propertyNameOrDisplayName) || displayName.equals(propertyNameOrDisplayName))
				return propertyDescriptor.getPropertyName();
		}
		throw new OneException("No property found with name: " + propertyNameOrDisplayName);
	}
	
}
