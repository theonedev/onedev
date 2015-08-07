package com.pmease.commons.wicket.behavior.menu;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;

@SuppressWarnings("serial")
public abstract class CheckItem extends MenuItem {

	@Override
	public final Component newContent(String componentId) {
		return new CheckComponent(componentId, this);
	}

	protected abstract String getLabel();
	
	protected abstract boolean isChecked();
	
	protected abstract void onClick(AjaxRequestTarget target);
	
	protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
		
	}
	
}
