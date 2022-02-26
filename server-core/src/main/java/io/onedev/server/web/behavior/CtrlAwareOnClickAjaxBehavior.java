package io.onedev.server.web.behavior;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;

public abstract class CtrlAwareOnClickAjaxBehavior extends AbstractDefaultAjaxBehavior {

	private static final long serialVersionUID = 1L;

	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		super.renderHead(component, response);
		String script = String.format("" 
				+ "$('#%s').click(function(e) {\n" 
				+ "  if (!e.ctrlKey && !e.metaKey) {\n" 
				+ "    %s\n"
				+ "    return false;\n" 
				+ "  }\n" 
				+ "});",
				component.getMarkupId(), getCallbackScript());
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

}
