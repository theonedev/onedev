package com.turbodev.server.web.util.ajaxlistener;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.attributes.IAjaxCallListener;

public class TrackViewStateListener implements IAjaxCallListener {

	private final boolean carryOver;
	
	public TrackViewStateListener(boolean carryOver) {
		this.carryOver = carryOver;
	}
	
	@Override
	public CharSequence getInitHandler(Component component) {
		return null;
	}

	@Override
	public CharSequence getBeforeHandler(Component component) {
		String script = "turbodev.server.viewState.getFromViewAndSetToHistory();";
		if (carryOver) {
			script += "turbodev.server.viewState.carryOver = turbodev.server.viewState.getFromHistory();";
		} else {
			script += "turbodev.server.viewState.carryOver = undefined;";
		}
		return script;
	}

	@Override
	public CharSequence getPrecondition(Component component) {
		return null;
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
		return ""
				+ "if (turbodev.server.viewState.carryOver)"
				+ "  turbodev.server.viewState.setToView(turbodev.server.viewState.carryOver);";
	}

	@Override
	public CharSequence getDoneHandler(Component component) {
		return null;
	}

}
