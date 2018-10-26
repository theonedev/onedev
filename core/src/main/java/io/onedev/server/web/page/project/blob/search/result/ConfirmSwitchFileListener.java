package io.onedev.server.web.page.project.blob.search.result;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.attributes.IAjaxCallListener;

class ConfirmSwitchFileListener implements IAjaxCallListener {

	private final String path;
	
	private final boolean hasMark;
	
	public ConfirmSwitchFileListener() {
		path = null;
		hasMark = false;
	}
	
	public ConfirmSwitchFileListener(String path, boolean hasMark) {
		this.path = path;
		this.hasMark = hasMark;
	}
	
	@Override
	public CharSequence getBeforeHandler(Component component) {
		return null;
	}

	@Override
	public CharSequence getPrecondition(Component component) {
		if (path != null) {
			return String.format("return onedev.server.searchResult.confirmSwitchFileByPath('%s', %b);", 
					StringEscapeUtils.escapeEcmaScript(path), hasMark);
		} else {
			return String.format("return onedev.server.searchResult.confirmSwitchFileByLink('%s');", 
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