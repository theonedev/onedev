package com.pmease.commons.wicket.behavior;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;

import com.pmease.commons.wicket.assets.uri.URIResourceReference;

@SuppressWarnings("serial")
public class ViewStateAwareBehavior extends Behavior {

	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		super.renderHead(component, response);
		
		response.render(JavaScriptHeaderItem.forReference(new URIResourceReference()));
		String script = String.format(""
				+ "var viewState = pmease.commons.history.getViewState();"
				+ "if (viewState) {"
				+ "  var link = document.getElementById('%s');"
				+ "  var uri = new URI(link);"
				+ "  uri.removeSearch('view_state');"
				+ "  uri.addSearch('view_state', JSON.stringify(viewState));"
				+ "  $(link).attr('href', uri.href());"
				+ "}", component.getMarkupId(true));
		response.render(OnDomReadyHeaderItem.forScript(script));
	}
	
}
