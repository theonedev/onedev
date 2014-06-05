package com.pmease.commons.editable;

import java.io.Serializable;
import java.lang.reflect.Method;

public interface PropertyDescriptor extends Serializable {
	
	public Class<?> getBeanClass();
	
	public String getPropertyName();
	
	public Method getPropertyGetter();
	
	public Method getPropertySetter();
	
	public Object getPropertyValue(Object bean);
	
	public void setPropertyValue(Object bean, Object propertyValue);
	
	public void copyProperty(Object from, Object to);
}
