package com.pmease.gitop.web.common.component.messenger;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

@SuppressWarnings("serial")
public class MessengerBehavior extends Behavior {

	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		super.renderHead(component, response);
		
		response.render(JavaScriptHeaderItem.forReference(newJsReference("res/js/messenger.js")));
		response.render(JavaScriptHeaderItem.forReference(newJsReference("res/js/messenger-theme-flat.js")));
		
		response.render(CssHeaderItem.forReference(newCssReference("res/css/messenger.css")));
		response.render(CssHeaderItem.forReference(newCssReference("res/css/messenger-theme-flat.css")));
	}
	
	private static ResourceReference newJsReference(String url) {
		return new JavaScriptResourceReference(MessengerBehavior.class, url);
	}
	
	private static ResourceReference newCssReference(String url) {
		return new CssResourceReference(MessengerBehavior.class, url);
	}
}
