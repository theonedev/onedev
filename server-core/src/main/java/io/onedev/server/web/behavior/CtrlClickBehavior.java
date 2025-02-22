package io.onedev.server.web.behavior;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;

public class CtrlClickBehavior extends Behavior {

	private Component delegate;
	
	public CtrlClickBehavior(Component delegate) {
		this.delegate = delegate;
		delegate.add(AttributeAppender.append("class", "d-none"));
	}
	
	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		super.renderHead(component, response);
		String script = String.format(""
				+ "$('#%s').click(function(e) {\n"
				+ "  if (!e.ctrlKey && !e.metaKey) {"
				+ "      $('#%s').click();\n"
				+ "    return false;\n"
				+ "  }\n"
				+ "});", 
				component.getMarkupId(), delegate.getMarkupId());
		response.render(OnDomReadyHeaderItem.forScript(script));
	}
	
}
