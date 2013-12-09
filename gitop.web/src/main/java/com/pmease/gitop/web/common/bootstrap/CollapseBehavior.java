package com.pmease.gitop.web.common.bootstrap;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;

public class CollapseBehavior extends Behavior {

	private static final long serialVersionUID = 1L;
	
	private final Component collapseComponent;
	
	public CollapseBehavior(Component collapseComponent) {
		this.collapseComponent = collapseComponent;
	}
	
	@Override
	public void bind(Component component) {
		component.add(AttributeModifier.replace("data-toggle", "collapse"));
		component.add(AttributeModifier.replace("data-target", "#" + collapseComponent.getMarkupId(true)));
	}
	
}
