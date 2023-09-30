package io.onedev.server.util;

import io.onedev.server.web.editable.BeanDescriptor;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.util.WicketUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

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
