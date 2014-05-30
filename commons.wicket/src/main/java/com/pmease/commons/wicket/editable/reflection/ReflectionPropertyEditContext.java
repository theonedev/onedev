package com.pmease.commons.wicket.editable.reflection;

import java.io.Serializable;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;

import com.pmease.commons.wicket.editable.AbstractReflectionPropertyEditContext;
import com.pmease.commons.wicket.editable.EditContext;

@SuppressWarnings("serial")
public class ReflectionPropertyEditContext extends AbstractReflectionPropertyEditContext {

	public ReflectionPropertyEditContext(Serializable bean, String propertyName) {
		super(bean, propertyName);
	}

	@Override
	public Component renderForEdit(String componentId) {
		return new ReflectionPropertyEditor(componentId, this);
	}

	@Override
	public Component renderForView(String componentId) {
		EditContext valueContext = getValueContext();
		if (valueContext != null) {
			return valueContext.renderForView(componentId);
		} else {
			return new Label(componentId, "<i>Not Defined</i>").setEscapeModelStrings(false);
		}
	}

}
