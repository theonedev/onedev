package com.pmease.commons.editable;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import com.pmease.commons.util.BeanUtils;

@SuppressWarnings("serial")
public class PropertyDescriptorImpl implements PropertyDescriptor {

	private final Class<? extends Serializable> beanClass;
	
	private final String propertyName;
	
	private transient Method propertyGetter;
	
	private transient Method propertySetter;
	
	public PropertyDescriptorImpl(Class<? extends Serializable> beanClass, String propertyName) {
		this.beanClass = beanClass;
		this.propertyName = propertyName;
	}
	
	@SuppressWarnings("unchecked")
	public PropertyDescriptorImpl(Method propertyGetter) {
		this.beanClass = (Class<? extends Serializable>) propertyGetter.getDeclaringClass();
		this.propertyName = BeanUtils.getPropertyName(propertyGetter);
		this.propertyGetter = propertyGetter;
	}
	
	@SuppressWarnings("unchecked")
	public PropertyDescriptorImpl(Method propertyGetter, Method propertySetter) {
		this.beanClass = (Class<? extends Serializable>) propertyGetter.getDeclaringClass();
		this.propertyName = BeanUtils.getPropertyName(propertyGetter);
		this.propertyGetter = propertyGetter;
		this.propertySetter = propertySetter;
	}

	public PropertyDescriptorImpl(PropertyDescriptor propertyDescriptor) {
		this.beanClass = propertyDescriptor.getBeanClass();
		this.propertyName = propertyDescriptor.getPropertyName();
		this.propertyGetter = propertyDescriptor.getPropertyGetter();
		this.propertySetter = propertyDescriptor.getPropertySetter();
	}
	
	@Override
	public Class<? extends Serializable> getBeanClass() {
		return beanClass;
	}

	@Override
	public String getPropertyName() {
		return propertyName;
	}

	@Override
	public Method getPropertyGetter() {
		if (propertyGetter == null)
			propertyGetter = BeanUtils.getGetter(beanClass, propertyName);
		return propertyGetter;
	}
	
	public Method getPropertySetter() {
		if (propertySetter == null)
			propertySetter = BeanUtils.getSetter(getPropertyGetter());
		return propertySetter;
	}

	@Override
	public void copyProperty(Serializable fromBean, Serializable toBean) {
		setPropertyValue(toBean, getPropertyValue(fromBean));
	}

	@Override
	public Serializable getPropertyValue(Serializable bean) {
		try {
			return (Serializable) getPropertyGetter().invoke(bean);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setPropertyValue(Serializable bean, Serializable propertyValue) {
		try {
			getPropertySetter().invoke(bean, propertyValue);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends Serializable> getPropertyClass() {
		return (Class<? extends Serializable>) getPropertyGetter().getReturnType();
	}

	@Override
	public boolean isPropertyRequired() {
		if (propertyGetter.getReturnType().isPrimitive()
				|| propertyGetter.getAnnotation(NotNull.class) != null 
				|| propertyGetter.getAnnotation(NotEmpty.class) != null) { 
			return true;
		} else {
			return false;
		}
	}

}
