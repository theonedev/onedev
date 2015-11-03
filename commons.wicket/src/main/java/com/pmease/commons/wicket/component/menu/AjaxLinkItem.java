package com.pmease.commons.wicket.component.menu;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;

@SuppressWarnings("serial")
public abstract class AjaxLinkItem extends MenuItem {
	
	private final String label;
	
	public AjaxLinkItem(String label) {
		this.label = label;
	}
	
	@Override
	public Component newContent(String componentId) {
		return new AjaxLinkComponent(componentId, label) {

			@Override
			protected void onClick(AjaxRequestTarget target) {
				AjaxLinkItem.this.onClick(target);
			}
			
		};
	}

	public abstract void onClick(AjaxRequestTarget target);
	
}
