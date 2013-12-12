package com.pmease.gitop.web.common.wicket.component.messenger;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import com.google.common.collect.ImmutableMap;
import com.pmease.gitop.web.common.util.JsUtil;

@SuppressWarnings("serial")
public class MessengerResourcesBehavior extends Behavior {

	private static final MessengerResourcesBehavior instance = new MessengerResourcesBehavior();
	
	public final static MessengerResourcesBehavior get() {
		return instance;
	}
	
	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		super.renderHead(component, response);
		
		response.render(JavaScriptHeaderItem.forReference(newJsReference("res/js/messenger.js")));
		response.render(JavaScriptHeaderItem.forReference(newJsReference("res/js/messenger-theme-flat.js")));
		
		response.render(CssHeaderItem.forReference(newCssReference("res/css/messenger.css")));
		response.render(CssHeaderItem.forReference(newCssReference("res/css/messenger-theme-flat.css")));
		
		response.render(JavaScriptHeaderItem.forScript(String.format(
				"Messenger.options = %s", getMessengerOptions()), 
				"global-messenger-init"));
	}
	
	protected String getMessengerOptions() {
		return 
				JsUtil.formatOptions(ImmutableMap.<String, Object>builder()
				.put("theme", "flat")
				.put("extraClasses", "messenger-fixed messenger-on-top messenger-on-right")
				.build());
	}
	
	private static ResourceReference newJsReference(String url) {
		return new JavaScriptResourceReference(MessengerResourcesBehavior.class, url);
	}
	
	private static ResourceReference newCssReference(String url) {
		return new CssResourceReference(MessengerResourcesBehavior.class, url);
	}
}
