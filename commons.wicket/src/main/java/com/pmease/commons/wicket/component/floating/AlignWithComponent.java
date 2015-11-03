package com.pmease.commons.wicket.component.floating;

import org.apache.wicket.Component;

public class AlignWithComponent implements AlignWith {

	private final Component component;
	
	public AlignWithComponent(Component component) {
		this.component = component;
	}
	
	@Override
	public String toJSON() {
		return "'" + component.getMarkupId(true) + "'";
	}

}
