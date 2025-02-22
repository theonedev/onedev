package io.onedev.server.web.behavior;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;

import io.onedev.server.web.asset.selectbytyping.SelectByTypingResourceReference;

public class SelectByTypingBehavior extends Behavior {

	private final Component container;
	
	public SelectByTypingBehavior(Component container) {
		this.container = container;
	}

	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		super.renderHead(component, response);

		response.render(JavaScriptHeaderItem.forReference(new SelectByTypingResourceReference()));
		String script = String.format("$('#%s').selectByTyping('#%s');",
				component.getMarkupId(true), container.getMarkupId(true));
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

}
