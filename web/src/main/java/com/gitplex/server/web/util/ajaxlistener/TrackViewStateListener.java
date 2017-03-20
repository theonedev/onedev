package com.gitplex.server.web.util.ajaxlistener;

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
		String script = "gitplex.server.viewState.getFromViewAndSetToHistory();";
		if (carryOver) {
			script += "gitplex.server.viewState.carryOver = gitplex.server.viewState.getFromHistory();";
		} else {
			script += "gitplex.server.viewState.carryOver = undefined;";
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
				+ "if (gitplex.server.viewState.carryOver)"
				+ "  gitplex.server.viewState.setToView(gitplex.server.viewState.carryOver);";
	}

	@Override
	public CharSequence getDoneHandler(Component component) {
		return null;
	}

}
