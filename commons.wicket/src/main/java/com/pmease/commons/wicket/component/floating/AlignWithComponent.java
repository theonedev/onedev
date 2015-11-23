package com.pmease.commons.wicket.component.floating;

import org.apache.wicket.Component;

public class AlignWithComponent implements AlignWith {

	private static final long serialVersionUID = 1L;
	
	private final Component component;
	
	private final int index;
	
	public AlignWithComponent(Component component, int index) {
		this.component = component;
		this.index = index;
		
		component.setOutputMarkupId(true);
	}
	
	public AlignWithComponent(Component component) {
		this(component, -1);
	}
	
	@Override
	public String toJSON() {
		return String.format("{element: '%s', index: %d}", component.getMarkupId(), index);
	}

}
