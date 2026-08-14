package io.onedev.server.web.component.savedquery;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.attributes.IAjaxCallListener;

class SavedQueriesDropdownListener implements IAjaxCallListener {

	private final String action;

	private SavedQueriesDropdownListener(String action) {
		this.action = action;
	}

	static SavedQueriesDropdownListener show() {
		return new SavedQueriesDropdownListener("show");
	}

	static SavedQueriesDropdownListener close() {
		return new SavedQueriesDropdownListener("close");
	}

	@Override
	public CharSequence getPrecondition(Component component) {
		return String.format("return onedev.server.savedQueries.%sDropdown(this);", action);
	}

	@Override
	public CharSequence getBeforeHandler(Component component) {
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
