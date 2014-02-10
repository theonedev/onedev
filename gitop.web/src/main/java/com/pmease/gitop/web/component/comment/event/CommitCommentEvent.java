package com.pmease.gitop.web.component.comment.event;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.pmease.gitop.model.CommitComment;
import com.pmease.gitop.web.common.wicket.event.AjaxEvent;

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
