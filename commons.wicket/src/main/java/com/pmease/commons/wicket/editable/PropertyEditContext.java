package com.pmease.commons.wicket.editable;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import com.pmease.commons.editable.EditableUtils;
import com.pmease.commons.util.BeanUtils;
import com.pmease.commons.util.ExceptionUtils;
import com.pmease.commons.util.ReflectionUtils;

@SuppressWarnings("serial")
public abstract class PropertyEditContext extends AbstractEditContext {
	
	private final String propertyName;
	
	private transient Method propertyGetter;
	
	private transient Method propertySetter;
	
	public PropertyEditContext(Serializable bean, String propertyName) {
		super(bean);
		this.propertyName = propertyName;
	}
	
	public String getPropertyName() {
		return propertyName;
	}
	
	public Method getPropertyGetter() {
		if (propertyGetter == null)
			propertyGetter = BeanUtils.getGetter(getBeanClass(), propertyName);
		return propertyGetter;
	}
	
	public Method getPropertySetter() {
		if (propertySetter == null)
			propertySetter = BeanUtils.getSetter(getPropertyGetter());
		return propertySetter;
	}
	
	public Serializable getPropertyValue() {
		try {
			return (Serializable) getPropertyGetter().invoke(getBean());
		} catch (Exception e) {
			throw ExceptionUtils.unchecked(e);
		}
	}
	
	public void setPropertyValue(Serializable propertyValue) {
		try {
			getPropertySetter().invoke(getBean(), propertyValue);
		} catch (Exception e) {
			throw ExceptionUtils.unchecked(e);
		}
	}
	
	public Serializable instantiate(Class<?> clazz) {
		try {
			Constructor<?> constructor = ReflectionUtils.findConstructor(clazz, getBeanClass());
			if (constructor != null)
				return (Serializable) constructor.newInstance(getBean());
			else
				return (Serializable) clazz.newInstance();
		} catch (Exception e) {
			throw ExceptionUtils.unchecked(e);
		}
	}
	
	public boolean isPropertyRequired() {
		return EditableUtils.isPropertyRequired(getPropertyGetter());
	}
	
}
