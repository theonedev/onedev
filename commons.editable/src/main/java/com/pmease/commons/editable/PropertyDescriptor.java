package com.pmease.commons.editable;

import java.io.Serializable;
import java.lang.reflect.Method;

public interface PropertyDescriptor extends Serializable {
	
	Class<? extends Serializable> getBeanClass();
	
	String getPropertyName();
	
	Method getPropertyGetter();
	
	Method getPropertySetter();
	
	Class<? extends Serializable> getPropertyClass();
	
	Serializable getPropertyValue(Serializable bean);
	
	boolean isPropertyRequired();
	
	void setPropertyValue(Serializable bean, Serializable propertyValue);
	
	void copyProperty(Serializable fromBean, Serializable toBean);
	
}
