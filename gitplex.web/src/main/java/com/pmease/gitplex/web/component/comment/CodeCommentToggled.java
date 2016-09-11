package com.pmease.gitplex.web.component.comment;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.pmease.commons.wicket.AjaxEvent;

public class CodeCommentToggled extends AjaxEvent {

	public CodeCommentToggled(AjaxRequestTarget target) {
		super(target);
	}

}
