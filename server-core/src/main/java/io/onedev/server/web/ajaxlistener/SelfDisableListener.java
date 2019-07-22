package io.onedev.server.web.ajaxlistener;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.attributes.IAjaxCallListener;

public class SelfDisableListener implements IAjaxCallListener {

	@Override
	public CharSequence getInitHandler(Component component) {
		return null;
	}

	@Override
	public CharSequence getBeforeHandler(Component component) {
		return null;
	}

	@Override
	public CharSequence getPrecondition(Component component) {
		return null;
	}

	@Override
	public CharSequence getBeforeSendHandler(Component component) {
		return String.format("$('#%s').attr('disabled', 'disabled');", component.getMarkupId(true));
	}

	@Override
	public CharSequence getAfterHandler(Component component) {
		return null;
	}

	@Override
	public CharSequence getSuccessHandler(Component component) {
		return String.format("$('#%s').removeAttr('disabled');", component.getMarkupId(true));
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
	public CharSequence getDoneHandler(Component component) {
		return null;
	}

}
