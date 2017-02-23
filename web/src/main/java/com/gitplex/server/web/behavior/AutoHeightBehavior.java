package com.gitplex.server.web.behavior;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;

@SuppressWarnings("serial")
public class AutoHeightBehavior extends Behavior {

	private final int bottomOffset;
	
	public AutoHeightBehavior(int bottomOffset) {
		this.bottomOffset = bottomOffset;
	}
	
	@Override
	public void bind(Component component) {
		super.bind(component);
		component.setOutputMarkupId(true);
	}

	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		super.renderHead(component, response);

		String script = String.format("gitplex.server.autoHeight('#%s', %d);", 
					component.getMarkupId(), bottomOffset);
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

}
