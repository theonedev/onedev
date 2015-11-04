package com.pmease.commons.wicket.component.floating;

import org.apache.wicket.Component;

public class AlignFloatingWithComponent implements AlignFloatingWith {

	private static final long serialVersionUID = 1L;
	
	private final Component component;
	
	public AlignFloatingWithComponent(Component component) {
		this.component = component;
		component.setOutputMarkupId(true);
	}
	
	@Override
	public String toJSON() {
		return "'" + component.getMarkupId() + "'";
	}

}
