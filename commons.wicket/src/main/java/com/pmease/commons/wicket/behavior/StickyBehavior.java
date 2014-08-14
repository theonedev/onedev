package com.pmease.commons.wicket.behavior;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;

import com.pmease.commons.wicket.jquery.FunctionWithParams;

import de.agilecoders.wicket.jquery.JQuery;

@SuppressWarnings("serial")
public class StickyBehavior extends Behavior {

	@Override
	public void bind(Component component) {
		super.bind(component);
		component.setOutputMarkupId(true);
	}

	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		super.renderHead(component, response);
		
		String script = JQuery.$(component)
				.chain(new FunctionWithParams("stick_in_parent"))
				.get();
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

}
