package io.onedev.server.web.editable;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.Sets;

import io.onedev.commons.utils.BeanUtils;
import io.onedev.server.OneException;
import io.onedev.server.web.editable.annotation.Editable;

@SuppressWarnings("serial")
public class BeanDescriptor implements Serializable {

	private final Class<?> beanClass;
	
	protected final Map<String, List<PropertyDescriptor>> propertyDescriptors;
	
	public BeanDescriptor(Class<?> beanClass) {
		this(beanClass, Sets.newHashSet(), true);
	}
	
	public BeanDescriptor(Class<?> beanClass, Collection<String> properties, boolean excluded) {
		this.beanClass = beanClass;
		
		propertyDescriptors = new LinkedHashMap<>();

		List<Method> propertyGetters = BeanUtils.findGetters(getBeanClass());
		EditableUtils.sortAnnotatedElements(propertyGetters);
		
		for (Method propertyGetter: propertyGetters) {
			Editable editable = propertyGetter.getAnnotation(Editable.class);
			if (editable != null) {
				String group = editable.group();
				List<PropertyDescriptor> groupPropertyDescriptors = propertyDescriptors.get(group);
				if (groupPropertyDescriptors == null) {
					groupPropertyDescriptors = new ArrayList<>();
					propertyDescriptors.put(group, groupPropertyDescriptors);
				}
				PropertyDescriptor propertyDescriptor = new PropertyDescriptor(propertyGetter); 
				groupPropertyDescriptors.add(propertyDescriptor);
				String propertyName = BeanUtils.getPropertyName(propertyGetter);
				propertyDescriptor.setPropertyExcluded(BeanUtils.findSetter(propertyGetter) == null 
						|| properties.contains(propertyName) && excluded 
						|| !properties.contains(propertyName) && !excluded);
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

	public Map<String, List<PropertyDescriptor>> getPropertyDescriptors() {
		return propertyDescriptors;
	}
	
	public Map<String, PropertyDescriptor> getMapOfDisplayNameToPropertyDescriptor() {
		Map<String, PropertyDescriptor> propertyDescriptors = new HashMap<>();
		for (List<PropertyDescriptor> groupProperties: getPropertyDescriptors().values()) {
			for (PropertyDescriptor property: groupProperties)
				propertyDescriptors.put(EditableUtils.getDisplayName(property.getPropertyGetter()), property);
		}
		return propertyDescriptors;
	}

	public void copyProperties(Object from, Object to) {
		for (List<PropertyDescriptor> groupProperties: getPropertyDescriptors().values()) {
			for (PropertyDescriptor property: groupProperties) {
				if (!property.isPropertyExcluded())
					property.copyProperty(from, to);
			}
		}
	}
	
	@Nullable
	public PropertyDescriptor getPropertyDescriptor(String propertyName) {
		for (List<PropertyDescriptor> groupProperties: getPropertyDescriptors().values()) {
			for (PropertyDescriptor property: groupProperties) {
				if (property.getPropertyName().equals(propertyName))
					return property;
			}
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
		for (List<PropertyDescriptor> groupProperties: propertyDescriptors.values()) {
			for (PropertyDescriptor property: groupProperties) {
				String displayName = property.getDisplayName();
				if (property.getPropertyName().equals(propertyNameOrDisplayName) || displayName.equals(propertyNameOrDisplayName))
					return property.getPropertyName();
			}
		}
		throw new OneException("No property found with name: " + propertyNameOrDisplayName);
	}
	
}
