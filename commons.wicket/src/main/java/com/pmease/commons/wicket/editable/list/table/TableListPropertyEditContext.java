package com.pmease.commons.wicket.editable.list.table;

import java.io.Serializable;

import org.apache.wicket.markup.html.basic.Label;

import com.pmease.commons.editable.AbstractTableListPropertyEditContext;

@SuppressWarnings("serial")
public class TableListPropertyEditContext extends AbstractTableListPropertyEditContext {

	public TableListPropertyEditContext(Serializable bean, String propertyName) {
		super(bean, propertyName);
	}

	@Override
	public Object renderForEdit(Object renderParam) {
		return new TableListPropertyEditor((String) renderParam, this);
	}

	@Override
	public Object renderForView(Object renderParam) {
		if (getElementContexts() != null) {
			return new TableListPropertyViewer((String) renderParam, this);
		} else {
			return new Label((String) renderParam, "<i>Not Defined</i>").setEscapeModelStrings(false);
		}
	}

}
