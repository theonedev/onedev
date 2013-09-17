package com.pmease.commons.wicket.editable.reflection;

import java.io.Serializable;

import com.pmease.commons.editable.AbstractReflectionBeanEditContext;
import com.pmease.commons.wicket.editable.RenderContext;

@SuppressWarnings("serial")
public class ReflectionBeanEditContext extends AbstractReflectionBeanEditContext<RenderContext> {

	public ReflectionBeanEditContext(Serializable bean) {
		super(bean);
	}

	@Override
	public void renderForEdit(RenderContext renderContext) {
		renderContext.getContainer().add(new ReflectionBeanEditor(renderContext.getComponentId(), this));
	}

	@Override
	public void renderForView(RenderContext renderContext) {
		renderContext.getContainer().add(new ReflectionBeanViewer(renderContext.getComponentId(), this));
	}

}
