package com.pmease.commons.wicket.behavior.menu;

import org.apache.wicket.Component;

@SuppressWarnings("serial")
public abstract class CheckItem extends MenuItem {

	@Override
	public Component newContent(String componentId) {
		return new CheckComponent(componentId, this);
	}

	protected abstract String getLabel();
	
	protected abstract boolean isTicked();
	
	protected abstract void onClick();
	
}
