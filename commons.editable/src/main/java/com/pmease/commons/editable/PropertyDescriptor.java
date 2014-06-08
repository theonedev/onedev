package com.pmease.commons.editable;

import java.io.Serializable;
import java.lang.reflect.Method;

public interface PropertyDescriptor extends Serializable {
	
	Class<?> getBeanClass();
	
	String getPropertyName();
	
	Method getPropertyGetter();
	
	Method getPropertySetter();
	
	Class<?> getPropertyClass();
	
	Object getPropertyValue(Object bean);
	
	boolean isPropertyRequired();
	
	void setPropertyValue(Object bean, Object propertyValue);
	
	void copyProperty(Object fromBean, Object toBean);
	
}
