package com.pmease.commons.wicket.editable.polymorphic;

import java.io.Serializable;

import org.apache.wicket.markup.html.WebMarkupContainer;

import com.pmease.commons.editable.AbstractPolymorphicPropertyEditContext;
import com.pmease.commons.editable.EditContext;
import com.pmease.commons.wicket.editable.RenderContext;

@SuppressWarnings("serial")
public class PolymorphicPropertyEditContext extends AbstractPolymorphicPropertyEditContext<RenderContext> {

	public PolymorphicPropertyEditContext(Serializable bean, String propertyName) {
		super(bean, propertyName);
	}

	@Override
	public void renderForEdit(RenderContext renderContext) {
		renderContext.getContainer().add(new PolymorphicPropertyEditor(renderContext.getComponentId(), this));
	}

	@Override
	public void renderForView(RenderContext renderContext) {
		EditContext<RenderContext> valueContext = getValueContext();
		if (valueContext != null)
			valueContext.renderForView(renderContext);
		else
			renderContext.getContainer().add(new WebMarkupContainer(renderContext.getComponentId()));
	}

}
