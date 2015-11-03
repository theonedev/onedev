package com.pmease.commons.wicket.dropdown;

import org.apache.wicket.Component;

public class AlignWithComponent implements AlignWith {

	private final Component component;
	
	public AlignWithComponent(Component component) {
		this.component = component;
	}

	public Component getComponent() {
		return component;
	}
	
}
