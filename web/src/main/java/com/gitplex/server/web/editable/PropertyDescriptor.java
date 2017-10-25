package com.gitplex.server.web.editable;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;

import com.gitplex.utils.BeanUtils;

public class PropertyDescriptor implements Serializable {

	private static final long serialVersionUID = 1L;

	private final Class<?> beanClass;
	
	private final String propertyName;
	
	private transient Method propertyGetter;
	
	private transient Method propertySetter;
	
	public PropertyDescriptor(Class<?> beanClass, String propertyName) {
		this.beanClass = beanClass;
		this.propertyName = propertyName;
	}
	
	public PropertyDescriptor(Method propertyGetter) {
		this.beanClass = propertyGetter.getDeclaringClass();
		this.propertyName = BeanUtils.getPropertyName(propertyGetter);
		this.propertyGetter = propertyGetter;
	}
	
	public PropertyDescriptor(Method propertyGetter, Method propertySetter) {
		this.beanClass = propertyGetter.getDeclaringClass();
		this.propertyName = BeanUtils.getPropertyName(propertyGetter);
		this.propertyGetter = propertyGetter;
		this.propertySetter = propertySetter;
	}

	public PropertyDescriptor(PropertyDescriptor propertyDescriptor) {
		this.beanClass = propertyDescriptor.getBeanClass();
		this.propertyName = propertyDescriptor.getPropertyName();
		this.propertyGetter = propertyDescriptor.getPropertyGetter();
		this.propertySetter = propertyDescriptor.getPropertySetter();
	}
	
	public Class<?> getBeanClass() {
		return beanClass;
	}

	public String getPropertyName() {
		return propertyName;
	}

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

	public void copyProperty(Object fromBean, Object toBean) {
		setPropertyValue(toBean, getPropertyValue(fromBean));
	}

	public Object getPropertyValue(Object bean) {
		try {
			return getPropertyGetter().invoke(bean);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	public void setPropertyValue(Object bean, Object propertyValue) {
		try {
			getPropertySetter().invoke(bean, propertyValue);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	public Class<?> getPropertyClass() {
		return getPropertyGetter().getReturnType();
	}

	public boolean isPropertyRequired() {
		return getPropertyGetter().getReturnType().isPrimitive()
				|| getPropertyGetter().getAnnotation(NotNull.class) != null 
				|| getPropertyGetter().getAnnotation(NotEmpty.class) != null
				|| getPropertyGetter().getAnnotation(Size.class) != null && getPropertyGetter().getAnnotation(Size.class).min()>=1;
	}

}
