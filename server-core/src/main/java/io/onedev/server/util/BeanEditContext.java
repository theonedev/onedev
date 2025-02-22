package io.onedev.server.util;

import io.onedev.server.web.editable.BeanDescriptor;

public class BeanEditContext implements EditContext {
	
	private final Object bean;
	
	public BeanEditContext(Object bean) {
		this.bean = bean;
	}
	
	@Override
	public Object getInputValue(String name) {
		return new BeanDescriptor(bean.getClass()).getProperty(name).getPropertyValue(bean);
	}
	
}
