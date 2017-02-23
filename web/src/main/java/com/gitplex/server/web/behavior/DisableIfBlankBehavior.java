package com.gitplex.server.web.behavior;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;

@SuppressWarnings("serial")
public class DisableIfBlankBehavior extends Behavior {
	
	private Component button;
	
	public DisableIfBlankBehavior(Component button) {
		this.button = button;
		button.setOutputMarkupId(true);
	}

	@Override
	public void bind(Component component) {
		super.bind(component);
		component.setOutputMarkupId(true);
	}

	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		super.renderHead(component, response);
		
		String script = String.format("gitplex.server.disableIfBlank('%s', '%s');", 
				component.getMarkupId(), button.getMarkupId());
		response.render(OnDomReadyHeaderItem.forScript(script));
	}
	
}
