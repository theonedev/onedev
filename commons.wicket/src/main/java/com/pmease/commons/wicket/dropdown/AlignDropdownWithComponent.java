package com.pmease.commons.wicket.dropdown;

import org.apache.wicket.Component;

public class AlignDropdownWithComponent implements AlignDropdownWith {

	private static final long serialVersionUID = 1L;
	
	private final Component component;
	
	public AlignDropdownWithComponent(Component component) {
		this.component = component;
		component.setOutputMarkupId(true);
	}

	public Component getComponent() {
		return component;
	}
	
}
