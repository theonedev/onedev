package com.pmease.commons.wicket.editable.reflection;

import java.io.Serializable;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;

import com.pmease.commons.editable.AbstractReflectionPropertyEditContext;
import com.pmease.commons.wicket.editable.RenderableEditContext;

@SuppressWarnings("serial")
public class ReflectionPropertyEditContext extends AbstractReflectionPropertyEditContext implements RenderableEditContext {

	public ReflectionPropertyEditContext(Serializable bean, String propertyName) {
		super(bean, propertyName);
	}

	@Override
	public Component renderForEdit(String componentId) {
		return new ReflectionPropertyEditor(componentId, this);
	}

	@Override
	public Component renderForView(String componentId) {
		RenderableEditContext valueContext = (RenderableEditContext) getValueContext();
		if (valueContext != null) {
			return valueContext.renderForView(componentId);
		} else {
			return new WebMarkupContainer(componentId);
		}
	}

}
