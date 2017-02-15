package com.gitplex.server.web.editable;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.gitplex.server.web.util.AjaxEvent;

public class EditorChanged extends AjaxEvent {

	public EditorChanged(AjaxRequestTarget target) {
		super(target);
	}

}