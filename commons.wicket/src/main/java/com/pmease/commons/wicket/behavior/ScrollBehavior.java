package com.pmease.commons.wicket.behavior;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;

@SuppressWarnings("serial")
public class ScrollBehavior extends Behavior {

	private final Component fixContainer;
	
	private final String selector;
	
	private final int margin;
	
	private final boolean forward;
	
	public ScrollBehavior(Component fixContainer, String selector, int margin, boolean forward) {
		fixContainer.setOutputMarkupId(true);
		this.fixContainer = fixContainer;
		this.selector = selector;
		this.margin = margin;
		this.forward = forward;
	}
	
	@Override
	public void bind(Component component) {
		super.bind(component);
		
		component.setOutputMarkupId(true);
		
		String function;
		if (forward)
			function = "next";
		else
			function = "prev";
		String script = String.format("pmease.commons.scroll.%s('%s', '%s', %d);", 
				function, fixContainer.getMarkupId(), selector, margin);
		component.add(AttributeModifier.replace("onclick", script));
	}

	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		super.renderHead(component, response);
		
		String function;
		if (forward)
			function = "getNext";
		else
			function = "getPrev";
		
		String script = String.format("$(window).scroll(function() {"
				+ "var $component = $('#%s');"
				+ "if (pmease.commons.scroll.%s('%s', '%s', %d) == null)"
				+ "  $component.attr('disabled', 'disabled');"
				+ "else "
				+ "  $component.removeAttr('disabled');"
				+ "})", component.getMarkupId(), function, fixContainer.getMarkupId(), selector, margin);
		
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

}
