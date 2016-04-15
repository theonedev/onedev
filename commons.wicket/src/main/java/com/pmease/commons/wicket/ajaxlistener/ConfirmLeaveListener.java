package com.pmease.commons.wicket.ajaxlistener;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.attributes.IAjaxCallListener;

public class ConfirmLeaveListener implements IAjaxCallListener {

	private Component dirtyContainer;
	
	public ConfirmLeaveListener() {
	}
	
	public ConfirmLeaveListener(Component dirtyContainer) {
		this.dirtyContainer = dirtyContainer;
	}
	
	@Override
	public CharSequence getBeforeHandler(Component component) {
		return null;
	}

	@Override
	public CharSequence getPrecondition(Component component) {
		if (dirtyContainer != null)
			return String.format("return pmease.commons.form.confirmLeave('%s');", dirtyContainer.getMarkupId(true));
		else
			return "return pmease.commons.form.confirmLeave();";
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
