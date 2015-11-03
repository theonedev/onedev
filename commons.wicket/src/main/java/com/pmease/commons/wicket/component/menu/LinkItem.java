package com.pmease.commons.wicket.component.menu;

import org.apache.wicket.Component;

@SuppressWarnings("serial")
public abstract class LinkItem extends MenuItem {
	
	private final String label;
	
	public LinkItem(String label) {
		this.label = label;
	}
	
	@Override
	public Component newContent(String componentId) {
		return new LinkComponent(componentId, label) {

			@Override
			protected void onClick() {
				LinkItem.this.onClick();
			}
			
		};
	}

	public abstract void onClick();
	
}
