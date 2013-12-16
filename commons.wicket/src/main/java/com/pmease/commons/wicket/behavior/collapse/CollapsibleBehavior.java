package com.pmease.commons.wicket.behavior.collapse;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.behavior.Behavior;

@SuppressWarnings("serial")
class CollapsibleBehavior extends Behavior {

	Component trigger;
	
	Component target;
	
	CollapsibleBehavior(Component trigger) {
		this.trigger = trigger;
	}
	
	@Override
	public void bind(Component component) {
		super.bind(component);
		target = component;
		component.add(AttributeAppender.append("class", "collapse"));
	}

}
