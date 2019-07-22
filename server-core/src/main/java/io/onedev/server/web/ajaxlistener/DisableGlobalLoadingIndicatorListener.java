package io.onedev.server.web.ajaxlistener;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.attributes.IAjaxCallListener;

public class DisableGlobalLoadingIndicatorListener implements IAjaxCallListener {

	@Override
	public CharSequence getBeforeHandler(Component component) {
		return null;
	}

	@Override
	public CharSequence getPrecondition(Component component) {
		return null;
	}

	@Override
	public CharSequence getAfterHandler(Component component) {
		return ""
				+ "clearTimeout($('#ajax-loading-indicator')[0].timer);"
				+ "$('#ajax-loading-indicator')[0].timer = null;";
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

	@Override
	public CharSequence getBeforeSendHandler(Component component) {
		return null;
	}


}
