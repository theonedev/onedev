package io.onedev.server.web.component.floating;

import org.apache.wicket.Component;

public class ComponentTarget implements AlignTarget {
	
	private static final long serialVersionUID = 1L;
	
	private final Component component;
	
	private final int index;
	
	public ComponentTarget(Component component, int index) {
		this.component = component;
		this.index = index;
		
		component.setOutputMarkupId(true);
	}
	
	public ComponentTarget(Component component) {
		this(component, -1);
	}
	
	@Override
	public String toString() {
		return String.format("{element: document.getElementById('%s'), index: %s}", 
				component.getMarkupId(true), index!=-1?index:"undefined");
	}
	
}