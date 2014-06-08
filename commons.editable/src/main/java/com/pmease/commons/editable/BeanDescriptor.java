package com.pmease.commons.editable;

import java.io.Serializable;
import java.util.List;

public interface BeanDescriptor extends Serializable {

	Class<?> getBeanClass();
	
	Object newBeanInstance();
	
	List<PropertyDescriptor> getPropertyDescriptors();
	
	void copyProperties(Object from, Object to);
}
