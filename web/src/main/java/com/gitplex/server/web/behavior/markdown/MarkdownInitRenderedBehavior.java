package com.gitplex.server.web.behavior.markdown;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;

@SuppressWarnings("serial")
public class MarkdownInitRenderedBehavior extends Behavior {

	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		super.renderHead(component, response);
		response.render(JavaScriptHeaderItem.forReference(new MarkdownResourceReference()));
		
		String script = String.format("gitplex.server.markdown.initRendered('%s');", component.getMarkupId());
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

	@Override
	public void bind(Component component) {
		super.bind(component);
		component.setOutputMarkupId(true);
	}

}
