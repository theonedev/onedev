package com.turbodev.server.web.editable;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.turbodev.server.web.util.AjaxPayload;

public class EditorChanged extends AjaxPayload {

	public EditorChanged(AjaxRequestTarget target) {
		super(target);
	}

}