package io.onedev.server.web.editable;

import org.apache.wicket.ajax.AjaxRequestTarget;

import io.onedev.server.web.util.AjaxPayload;

public class EditorChanged extends AjaxPayload {

	public EditorChanged(AjaxRequestTarget target) {
		super(target);
	}

}