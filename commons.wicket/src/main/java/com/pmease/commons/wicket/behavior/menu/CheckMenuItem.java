package com.pmease.commons.wicket.behavior.menu;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;

@SuppressWarnings("serial")
public abstract class CheckMenuItem extends MenuItem {

	@Override
	public Component newContent(String componentId) {
		return new CheckMenuItemComponent(componentId, this);
	}

	protected abstract String getLabel();
	
	protected abstract IModel<Boolean> getCheckModel();
	
	protected abstract void onUpdate(AjaxRequestTarget target);
}
