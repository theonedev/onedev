package com.pmease.commons.wicket.editable.reflection;

import java.io.Serializable;

import com.pmease.commons.editable.AbstractReflectionBeanEditContext;

@SuppressWarnings("serial")
public class ReflectionBeanEditContext extends AbstractReflectionBeanEditContext {

	public ReflectionBeanEditContext(Serializable bean) {
		super(bean);
	}

	@Override
	public Object renderForEdit(Object renderParam) {
		return new ReflectionBeanEditor((String) renderParam, this);
	}

	@Override
	public Object renderForView(Object renderParam) {
		return new ReflectionBeanViewer((String) renderParam, this);
	}

}
