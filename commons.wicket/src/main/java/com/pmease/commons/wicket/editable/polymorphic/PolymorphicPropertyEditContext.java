package com.pmease.commons.wicket.editable.polymorphic;

import java.io.Serializable;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;

import com.pmease.commons.wicket.editable.AbstractPolymorphicPropertyEditContext;
import com.pmease.commons.wicket.editable.EditContext;

@SuppressWarnings("serial")
public class PolymorphicPropertyEditContext extends AbstractPolymorphicPropertyEditContext {

	public PolymorphicPropertyEditContext(Serializable bean, String propertyName) {
		super(bean, propertyName);
	}

	@Override
	public Component renderForEdit(String componentId) {
		return new PolymorphicPropertyEditor(componentId, this);
	}

	@Override
	public Component renderForView(String componentId) {
		EditContext valueContext = getValueContext();
		
		if (valueContext != null)
			return valueContext.renderForView(componentId);
		else
			return new Label(componentId, "<i>Not Defined</i>").setEscapeModelStrings(false);
	}

}
