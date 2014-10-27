package com.pmease.commons.wicket.behavior;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnLoadHeaderItem;

@SuppressWarnings("serial")
public class StickyBehavior extends Behavior {

	private Component component;
	
	private final Component parent;
	
	public StickyBehavior(@Nullable Component parent) {
		if (parent != null)
			parent.setOutputMarkupId(true);
		this.parent = parent;
	}
	
	public StickyBehavior() {
		this(null);
	}

	@Override
	public void bind(Component component) {
		super.bind(component);
		component.add(AttributeAppender.append("class", " sticky"));
		component.setOutputMarkupId(true);
		
		this.component = component;
	}

	public void restick(AjaxRequestTarget target) {
		target.prependJavaScript(String.format("$('#%s').trigger('sticky_kit:detach');", component.getMarkupId()));
		target.appendJavaScript(getStickScript());
	}
	
	private String getStickScript() {
		String script;
		if (parent != null) {
			script = String.format("pmease.commons.stick('#%s', '#%s');", 
					component.getMarkupId(), parent.getMarkupId());
		} else {
			script = String.format("pmease.commons.stick('#%s', undefined);", 
					component.getMarkupId());
		}
		return script;
	}
	
	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		super.renderHead(component, response);

		// execute this script OnLoad instead of OnDomReady here, as sticky
		// calculation will not be accurate if images are not loaded
		response.render(OnLoadHeaderItem.forScript(getStickScript()));
	}

}
