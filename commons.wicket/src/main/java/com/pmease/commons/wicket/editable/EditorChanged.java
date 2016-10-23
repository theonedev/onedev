package com.pmease.commons.wicket.editable;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.pmease.commons.wicket.AjaxEvent;

public class EditorChanged extends AjaxEvent {

	public EditorChanged(AjaxRequestTarget target) {
		super(target);
	}

}