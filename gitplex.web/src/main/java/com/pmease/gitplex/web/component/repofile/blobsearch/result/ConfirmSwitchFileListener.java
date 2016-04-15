package com.pmease.gitplex.web.component.repofile.blobsearch.result;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.attributes.IAjaxCallListener;

class ConfirmSwitchFileListener implements IAjaxCallListener {

	private final String path;
	
	public ConfirmSwitchFileListener() {
		path = null;
	}
	
	public ConfirmSwitchFileListener(String path) {
		this.path = path;
	}
	
	@Override
	public CharSequence getBeforeHandler(Component component) {
		return null;
	}

	@Override
	public CharSequence getPrecondition(Component component) {
		if (path != null) {
			return String.format("return gitplex.searchresult.confirmSwitchFileByPath('%s');", 
					StringEscapeUtils.escapeEcmaScript(path));
		} else {
			return String.format("return gitplex.searchresult.confirmSwitchFileByLink('%s');", 
					component.getMarkupId(true));
		}
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
	public CharSequence getBeforeSendHandler(Component component) {
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