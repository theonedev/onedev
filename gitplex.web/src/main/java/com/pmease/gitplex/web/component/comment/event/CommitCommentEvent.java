package com.pmease.gitplex.web.component.comment.event;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.pmease.gitplex.core.model.CommitComment;
import com.pmease.gitplex.web.common.wicket.event.AjaxEvent;

public abstract class CommitCommentEvent extends AjaxEvent {

	final CommitComment comment;
	
	public CommitCommentEvent(AjaxRequestTarget target, CommitComment comment) {
		super(target);
		
		this.comment = comment;
	}

	public CommitComment getComment() {
		return comment;
	}
}
