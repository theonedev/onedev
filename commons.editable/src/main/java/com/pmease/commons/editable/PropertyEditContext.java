package com.pmease.commons.editable;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import com.pmease.commons.loader.AppLoader;
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
			propertyGetter = BeanUtils.getGetter(getBean().getClass(), propertyName);
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
	
	protected void doValidation() {
		Validator validator = AppLoader.getInstance(Validator.class);
		for (ConstraintViolation<Serializable> violation: validator.validateProperty(getBean(), getPropertyName())) {
			error(violation.getMessage());
		} 
	}
	
	public Serializable instantiate(Class<?> clazz) {
		try {
			Constructor<?> constructor = ReflectionUtils.findConstructor(clazz, getBean().getClass());
			if (constructor != null)
				return (Serializable) constructor.newInstance(getBean());
			else
				return (Serializable) clazz.newInstance();
		} catch (Exception e) {
			throw ExceptionUtils.unchecked(e);
		}
	}
	
	public boolean isPropertyRequired() {
		if (getPropertyGetter().getReturnType().isPrimitive() 
				|| getPropertyGetter().getAnnotation(NotNull.class) != null 
				|| getPropertyGetter().getAnnotation(NotEmpty.class) != null) { 
			return true;
		} else {
			return false;
		}
	}
	
}
