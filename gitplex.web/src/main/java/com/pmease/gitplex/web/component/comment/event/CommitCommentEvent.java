package com.pmease.gitplex.web.component.comment.event;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.pmease.commons.wicket.AjaxEvent;
import com.pmease.gitplex.core.model.OldCommitComment;

public abstract class CommitCommentEvent extends AjaxEvent {

	final OldCommitComment comment;
	
	public CommitCommentEvent(AjaxRequestTarget target, OldCommitComment comment) {
		super(target);
		
		this.comment = comment;
	}

	public OldCommitComment getComment() {
		return comment;
	}
}
