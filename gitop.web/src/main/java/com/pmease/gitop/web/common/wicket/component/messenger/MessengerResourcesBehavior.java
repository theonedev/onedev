package com.pmease.gitop.web.common.wicket.component.messenger;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import com.pmease.commons.jackson.JsOptions;

@SuppressWarnings("serial")
public class MessengerResourcesBehavior extends Behavior {

	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		super.renderHead(component, response);
		
		response.render(JavaScriptHeaderItem.forReference(MessengerReference.instance()));
		response.render(JavaScriptHeaderItem.forScript(getInitScript(), "global-messenger-init"));
	}
	
	protected String getInitScript() {
		return String.format("Messenger.options = %s", getMessengerOptions());
	}
	
	protected String getMessengerOptions() {
		return new JsOptions()
					.put("theme", "flat")
					.put("extraClasses", "messenger-fixed messenger-on-top messenger-on-right")
					.toString();
				
	}
	
}
