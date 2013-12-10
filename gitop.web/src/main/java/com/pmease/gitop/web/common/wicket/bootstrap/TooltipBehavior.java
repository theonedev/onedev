package com.pmease.gitop.web.common.wicket.bootstrap;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.model.IModel;

public class TooltipBehavior extends Behavior {
	private static final long serialVersionUID = 1L;

	private final IModel<String> titleModel;
	
	public TooltipBehavior(IModel<String> titleModel) {
		this.titleModel = titleModel;
	}
	
	@Override
	public void bind(Component component) {
		component.add(AttributeModifier.replace("data-toggle", "tooltip"));
		component.add(AttributeModifier.replace("title", titleModel));
	}
}
