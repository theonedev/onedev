package com.gitplex.commons.wicket.behavior;

import org.apache.wicket.Component;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.google.common.base.Strings;

import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipConfig;

/**
 *
 */
public class TooltipBehavior extends de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipBehavior {
	private static final long serialVersionUID = 1L;
	
	public TooltipBehavior() {
		this(Model.of(""));
	}
	
	public TooltipBehavior(final TooltipConfig config) {
		this(Model.of(""), config);
	}
	
	public TooltipBehavior(IModel<String> label) {
		super(label);
	}
	
	public TooltipBehavior(final IModel<String> label, final TooltipConfig config) {
		super(label, config);
	}
	
	@Override
	public void onComponentTag(Component component, ComponentTag tag) {
		String original = tag.getAttribute("title");
		
		super.onComponentTag(component, tag);
		
		// hack here
		String current = tag.getAttribute("title");
		if (!Strings.isNullOrEmpty(original) && Strings.isNullOrEmpty(current)) {
			tag.put("title", original);
		}
	}
}