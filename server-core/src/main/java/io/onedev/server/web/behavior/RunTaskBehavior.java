package io.onedev.server.web.behavior;

import org.apache.wicket.ajax.AjaxRequestTarget;

@SuppressWarnings("serial")
public abstract class RunTaskBehavior extends AbstractPostAjaxBehavior {

	@Override
	protected void respond(AjaxRequestTarget target) {
		target.prependJavaScript(String.format(""
				+ "var $button = $('#%s');"
				+ "if ($button.length != 0) {"
				+ "$button.removeClass('disabled');"
				+ "$button.val($button[0].prevValue);"
				+ "$button.html($button[0].prevHtml);"
				+ "}",
				getComponent().getMarkupId()));
		runTask(target);
	}

	public void requestRun(AjaxRequestTarget target) {
		target.appendJavaScript(String.format(""
				+ "var $button = $('#%s');"
				+ "if ($button.length != 0) {"
				+ "$button.addClass('disabled');"
				+ "$button[0].prevValue = $button.val();"
				+ "$button[0].prevHtml = $button.html();"
				+ "$button.val($button.val() + ' in progress...');"
				+ "$button.html($button.html() + ' in progress...');"
				+ "}",
				getComponent().getMarkupId()));
		target.appendJavaScript(getCallbackScript());
	}
	
	protected abstract void runTask(AjaxRequestTarget target);
	
}
