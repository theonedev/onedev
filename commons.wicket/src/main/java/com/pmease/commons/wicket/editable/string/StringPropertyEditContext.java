package com.pmease.commons.wicket.editable.string;

import java.io.Serializable;
import java.util.Map;

import org.apache.wicket.markup.html.basic.Label;

import com.pmease.commons.editable.EditContext;
import com.pmease.commons.editable.PropertyEditContext;
import com.pmease.commons.wicket.editable.RenderContext;

@SuppressWarnings("serial")
public class StringPropertyEditContext extends PropertyEditContext<RenderContext> {

	public StringPropertyEditContext(Serializable bean, String propertyName) {
		super(bean, propertyName);
	}

	@Override
	public void renderForEdit(RenderContext renderContext) {
		renderContext.getContainer().add(new StringPropertyEditor(renderContext.getComponentId(), this));
	}

	@Override
	public void renderForView(RenderContext renderContext) {
		renderContext.getContainer().add(new Label(renderContext.getComponentId(), (String) getPropertyValue()));
	}

	@Override
	public Map<Serializable, EditContext<RenderContext>> getChildContexts() {
		return null;
	}

}
