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

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.util.BeanUtils;
import io.onedev.server.web.editable.annotation.Editable;

@SuppressWarnings("serial")
public class BeanDescriptor implements Serializable {

	private final Class<?> beanClass;
	
	protected final Map<String, List<PropertyDescriptor>> properties;
	
	public BeanDescriptor(Class<?> beanClass) {
		this(beanClass, Sets.newHashSet(), true);
	}
	
	public BeanDescriptor(Class<?> beanClass, Collection<String> propertyNames, boolean excluded) {
		this.beanClass = beanClass;
		
		properties = new LinkedHashMap<>();

		List<Method> propertyGetters = BeanUtils.findGetters(getBeanClass());
		EditableUtils.sortAnnotatedElements(propertyGetters);
		
		for (Method propertyGetter: propertyGetters) {
			Editable editable = propertyGetter.getAnnotation(Editable.class);
			if (editable != null) {
				String group = editable.group();
				List<PropertyDescriptor> groupProperties = properties.get(group);
				if (groupProperties == null) {
					groupProperties = new ArrayList<>();
					properties.put(group, groupProperties);
				}
				PropertyDescriptor property = new PropertyDescriptor(propertyGetter); 
				groupProperties.add(property);
				String propertyName = BeanUtils.getPropertyName(propertyGetter);
				property.setPropertyExcluded(BeanUtils.findSetter(propertyGetter) == null 
						|| propertyNames.contains(propertyName) && excluded 
						|| !propertyNames.contains(propertyName) && !excluded);
			}
		}
	}
	
	public BeanDescriptor(BeanDescriptor descriptor) {
		this.beanClass = descriptor.getBeanClass();
		this.properties = descriptor.getProperties();
	}
	
	public Class<?> getBeanClass() {
		return beanClass;
	}

	public Map<String, List<PropertyDescriptor>> getProperties() {
		return properties;
	}
	
	public Map<String, PropertyDescriptor> getMapOfDisplayNameToProperty() {
		Map<String, PropertyDescriptor> properties = new HashMap<>();
		for (List<PropertyDescriptor> groupProperties: getProperties().values()) {
			for (PropertyDescriptor property: groupProperties)
				properties.put(EditableUtils.getDisplayName(property.getPropertyGetter()), property);
		}
		return properties;
	}

	public void copyProperties(Object from, Object to) {
		for (List<PropertyDescriptor> groupProperties: getProperties().values()) {
			for (PropertyDescriptor property: groupProperties) {
				if (!property.isPropertyExcluded())
					property.copyProperty(from, to);
			}
		}
	}
	
	@Nullable
	public PropertyDescriptor getProperty(String propertyName) {
		for (List<PropertyDescriptor> groupProperties: getProperties().values()) {
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
		for (List<PropertyDescriptor> groupProperties: properties.values()) {
			for (PropertyDescriptor property: groupProperties) {
				String displayName = property.getDisplayName();
				if (property.getPropertyName().equals(propertyNameOrDisplayName) || displayName.equals(propertyNameOrDisplayName))
					return property.getPropertyName();
			}
		}
		throw new ExplicitException("No property found with name: " + propertyNameOrDisplayName);
	}
	
}
