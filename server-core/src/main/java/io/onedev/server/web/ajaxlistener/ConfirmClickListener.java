package io.onedev.server.web.ajaxlistener;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.attributes.IAjaxCallListener;

public class ConfirmClickListener implements IAjaxCallListener {

	private final String message;
	
	public ConfirmClickListener(String message) {
		this.message = message;
	}
	
	@Override
	public CharSequence getBeforeHandler(Component component) {
		return null;
	}

	@Override
	public CharSequence getPrecondition(Component component) {
		return String.format("return confirm('%s');", StringEscapeUtils.escapeEcmaScript(message));
	}

	@Override
	public CharSequence getBeforeSendHandler(Component component) {
		return null;
	}

	@Override
	public CharSequence getAfterHandler(Component component) {
		return null;
	}

	@Override
	public CharSequence getSuccessHandler(Component component) {
		return null;
	}

	@Override
	public CharSequence getFailureHandler(Component component) {
		return null;
	}

	@Override
	public CharSequence getCompleteHandler(Component component) {
		return null;
	}

	@Override
	public CharSequence getInitHandler(Component component) {
		return null;
	}

	@Override
	public CharSequence getDoneHandler(Component component) {
		return null;
	}

}
