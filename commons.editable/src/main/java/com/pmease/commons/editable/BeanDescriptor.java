package com.pmease.commons.editable;

import java.io.Serializable;
import java.util.List;

public interface BeanDescriptor<T extends Serializable> extends Serializable {

	Class<? extends T> getBeanClass();
	
	T newBeanInstance();
	
	List<PropertyDescriptor> getPropertyDescriptors();
	
	void copyProperties(Serializable from, Serializable to);
}
