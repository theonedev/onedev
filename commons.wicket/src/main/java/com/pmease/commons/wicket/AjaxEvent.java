package com.pmease.commons.wicket;

import org.apache.wicket.ajax.AjaxRequestTarget;

public class AjaxEvent {
	private final AjaxRequestTarget target;
	
	public AjaxEvent(AjaxRequestTarget target) {
		this.target = target;
	}

	public AjaxRequestTarget getTarget() {
		return target;
	}
}
