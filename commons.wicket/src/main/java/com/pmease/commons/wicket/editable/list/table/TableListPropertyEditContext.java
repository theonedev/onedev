package com.pmease.commons.wicket.editable.list.table;

import java.io.Serializable;

import com.pmease.commons.editable.AbstractTableListPropertyEditContext;
import com.pmease.commons.wicket.editable.RenderContext;

@SuppressWarnings("serial")
public class TableListPropertyEditContext extends AbstractTableListPropertyEditContext<RenderContext> {

	public TableListPropertyEditContext(Serializable bean, String propertyName) {
		super(bean, propertyName);
	}

	@Override
	public void renderForEdit(RenderContext renderContext) {
		renderContext.getContainer().add(new TableListPropertyEditor(renderContext.getComponentId(), this));
	}

	@Override
	public void renderForView(RenderContext renderContext) {
		renderContext.getContainer().add(new TableListPropertyViewer(renderContext.getComponentId(), this));
	}

}
