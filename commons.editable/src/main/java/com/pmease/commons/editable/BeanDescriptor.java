package com.pmease.commons.editable;

import java.io.Serializable;
import java.util.List;

public interface BeanDescriptor<T> extends Serializable {

	Class<? extends T> getBeanClass();
	
	List<PropertyDescriptor> getPropertyDescriptors();
	
	void copyProperties(Object from, Object to);
}
