package com.pmease.gitplex.web.event;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.pmease.gitplex.core.model.CommitComment;
import com.pmease.gitplex.web.common.wicket.event.AjaxEvent;

public class CommitCommentRemoved extends AjaxEvent {

	private final CommitComment comment;
	
	public CommitCommentRemoved(AjaxRequestTarget target, CommitComment comment) {
		super(target);
		
		this.comment = comment;
	}

	public CommitComment getComment() {
		return comment;
	}

}
