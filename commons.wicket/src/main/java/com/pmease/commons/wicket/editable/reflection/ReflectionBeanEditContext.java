package com.pmease.commons.wicket.editable.reflection;

import java.io.Serializable;

import org.apache.wicket.Component;

import com.pmease.commons.editable.AbstractReflectionBeanEditContext;
import com.pmease.commons.wicket.editable.RenderableEditContext;

@SuppressWarnings("serial")
public class ReflectionBeanEditContext extends AbstractReflectionBeanEditContext implements RenderableEditContext {

	public ReflectionBeanEditContext(Serializable bean) {
		super(bean);
	}

	@Override
	public Component renderForEdit(String componentId) {
		return new ReflectionBeanEditor(componentId, this);
	}

	@Override
	public Component renderForView(String componentId) {
		return new ReflectionBeanViewer(componentId, this);
	}

}
