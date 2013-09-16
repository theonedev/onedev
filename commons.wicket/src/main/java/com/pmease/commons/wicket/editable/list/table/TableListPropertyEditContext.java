package com.pmease.commons.wicket.editable.list.table;

import java.io.Serializable;

import org.apache.wicket.Component;

import com.pmease.commons.editable.AbstractTableListPropertyEditContext;
import com.pmease.commons.wicket.editable.RenderableEditContext;

@SuppressWarnings("serial")
public class TableListPropertyEditContext extends AbstractTableListPropertyEditContext implements RenderableEditContext {

	public TableListPropertyEditContext(Serializable bean, String propertyName) {
		super(bean, propertyName);
	}

	@Override
	public Component renderForEdit(String componentId) {
		return new TableListPropertyEditor(componentId, this);
	}

	@Override
	public Component renderForView(String componentId) {
		return new TableListPropertyViewer(componentId, this);
	}

}
