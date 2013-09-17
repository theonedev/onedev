package com.pmease.commons.wicket.editable.reflection;

import java.io.Serializable;

import org.apache.wicket.markup.html.WebMarkupContainer;

import com.pmease.commons.editable.AbstractReflectionPropertyEditContext;
import com.pmease.commons.editable.EditContext;
import com.pmease.commons.wicket.editable.RenderContext;

@SuppressWarnings("serial")
public class ReflectionPropertyEditContext extends AbstractReflectionPropertyEditContext<RenderContext> {

	public ReflectionPropertyEditContext(Serializable bean, String propertyName) {
		super(bean, propertyName);
	}

	@Override
	public void renderForEdit(RenderContext renderContext) {
		renderContext.getContainer().add(new ReflectionPropertyEditor(renderContext.getComponentId(), this));
	}

	@Override
	public void renderForView(RenderContext renderContext) {
		EditContext<RenderContext> valueContext = getValueContext();
		if (valueContext != null) {
			valueContext.renderForView(renderContext);
		} else {
			renderContext.getContainer().add(new WebMarkupContainer(renderContext.getComponentId()));
		}
	}

}
