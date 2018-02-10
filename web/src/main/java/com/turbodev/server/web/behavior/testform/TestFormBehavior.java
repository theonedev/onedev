package com.turbodev.server.web.behavior.testform;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.unbescape.html.HtmlEscape;
import org.unbescape.javascript.JavaScriptEscape;

import com.turbodev.server.web.behavior.AbstractPostAjaxBehavior;

@SuppressWarnings("serial")
public abstract class TestFormBehavior extends AbstractPostAjaxBehavior {

	@Override
	protected void respond(AjaxRequestTarget target) {
		String feedbackHtml;
		TestResult result = test();
		if (result.isSuccessful()) {
			feedbackHtml = String.format(
					"<div class='test-feedback alert alert-success'>%s</div>", 
					HtmlEscape.escapeHtml5(result.getMessage()));					
		} else {
			feedbackHtml = String.format(
					"<div class='test-feedback alert alert-danger'>%s</div>", 
					HtmlEscape.escapeHtml5(result.getMessage()));					
		} 
		feedbackHtml = StringUtils.replace(feedbackHtml, "\n", "<br>");
		target.appendJavaScript(String.format("var $button = $('#%s');"
				+ "$button.removeAttr('disabled');"
				+ "$button.val($button[0].prevValue);"
				+ "$button.html($button[0].prevHtml);"
				+ "$button.closest('form').append('%s');"
				+ "$button.removeClass('ajax-indicator');", 
				getComponent().getMarkupId(), JavaScriptEscape.escapeJavaScript(feedbackHtml)));
	}

	public void requestTest(AjaxRequestTarget target) {
		target.appendJavaScript(String.format("var $button = $('#%s');"
				+ "$button.attr('disabled', 'disabled');"
				+ "$button[0].prevValue = $button.val();"
				+ "$button[0].prevHtml = $button.html();"
				+ "$button.val($button.val() + ' in progress...');"
				+ "$button.html($button.html() + ' in progress...');"
				+ "$button.addClass('ajax-indicator');"
				+ "$button.closest('form').children('.test-feedback').remove();", 
				getComponent().getMarkupId()));
		target.appendJavaScript(getCallbackScript());
	}
	
	protected abstract TestResult test();
	
}
