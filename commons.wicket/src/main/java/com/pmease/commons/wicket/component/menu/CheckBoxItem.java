package com.pmease.commons.wicket.component.menu;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;

@SuppressWarnings("serial")
public abstract class CheckBoxItem extends MenuItem {

	@Override
	public Component newContent(String componentId) {
		return new CheckBoxComponent(componentId, this);
	}

	protected abstract String getLabel();
	
	protected abstract IModel<Boolean> getCheckModel();
	
	protected abstract void onUpdate(AjaxRequestTarget target);
}
