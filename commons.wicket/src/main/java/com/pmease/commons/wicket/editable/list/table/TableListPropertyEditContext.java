package com.pmease.commons.wicket.editable.list.table;

import java.io.Serializable;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;

import com.pmease.commons.wicket.editable.AbstractTableListPropertyEditContext;

@SuppressWarnings("serial")
public class TableListPropertyEditContext extends AbstractTableListPropertyEditContext {

	public TableListPropertyEditContext(Serializable bean, String propertyName) {
		super(bean, propertyName);
	}

	@Override
	public Component renderForEdit(String componentId) {
		return new TableListPropertyEditor(componentId, this);
	}

	@Override
	public Component renderForView(String componentId) {
		if (getElementContexts() != null) {
			return new TableListPropertyViewer(componentId, this);
		} else {
			return new Label(componentId, "<i>Not Defined</i>").setEscapeModelStrings(false);
		}
	}

}
