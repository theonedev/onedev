package com.pmease.commons.wicket.component.floating;

import org.apache.wicket.Component;

public class AlignWithComponent implements AlignWith {

	private static final long serialVersionUID = 1L;
	
	private final Component component;
	
	public AlignWithComponent(Component component) {
		this.component = component;
		component.setOutputMarkupId(true);
	}
	
	@Override
	public String toJSON() {
		return "'" + component.getMarkupId() + "'";
	}

}
