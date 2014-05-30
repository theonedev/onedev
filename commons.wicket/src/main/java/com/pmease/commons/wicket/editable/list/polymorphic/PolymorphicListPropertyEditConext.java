package com.pmease.commons.wicket.editable.list.polymorphic;

import java.io.Serializable;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;

import com.pmease.commons.wicket.editable.AbstractPolymorphicListPropertyEditContext;

@SuppressWarnings("serial")
public class PolymorphicListPropertyEditConext extends AbstractPolymorphicListPropertyEditContext {

	public PolymorphicListPropertyEditConext(Serializable bean, String propertyName) {
		super(bean, propertyName);
	}

	@Override
	public Component renderForEdit(String componentId) {
		return new PolymorphicListPropertyEditor(componentId, this);
	}

	@Override
	public Component renderForView(String componentId) {
		if (getElementContexts() != null) {
			return new PolymorphicListPropertyViewer(componentId, this);
		} else {
			return new Label(componentId, "<i>Not Defined</i>").setEscapeModelStrings(false);
		}
	}

}
