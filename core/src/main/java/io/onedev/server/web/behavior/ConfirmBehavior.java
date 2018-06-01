package io.onedev.server.web.behavior;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.unbescape.javascript.JavaScriptEscape;

@SuppressWarnings("serial")
public abstract class ConfirmBehavior extends AbstractPostAjaxBehavior {

	public void confirm(AjaxRequestTarget target, String message) {
		target.appendJavaScript(String.format("if (confirm('%s')) {%s}", 
				JavaScriptEscape.escapeJavaScript(message), getCallbackScript()));
		target.appendJavaScript(getCallbackScript());
	}
	
}
