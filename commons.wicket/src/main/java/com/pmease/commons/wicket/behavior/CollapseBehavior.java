package com.pmease.commons.wicket.behavior;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.IHeaderResponse;

import com.pmease.commons.wicket.asset.bootstrap.BootstrapHeaderItem;

@SuppressWarnings("serial")
public class CollapseBehavior extends Behavior {
	
	private Component target;

	public CollapseBehavior(Component target, boolean visibleInitially) {
		this.target = target;
		target.setOutputMarkupId(true);
		if (visibleInitially)
			target.add(AttributeAppender.append("class", "collapse in"));
		else
			target.add(AttributeAppender.append("class", "collapse"));
	}

	@Override
	public void bind(Component component) {
		super.bind(component);

		String script = String.format("$('#%s').collapse('toggle');", target.getMarkupId());
		component.add(AttributeModifier.replace("onclick", script));
	}

	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		super.renderHead(component, response);
		
		response.render(BootstrapHeaderItem.get());
	}
	
}
