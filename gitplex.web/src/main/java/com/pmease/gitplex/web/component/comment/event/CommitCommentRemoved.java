package com.pmease.gitplex.web.component.comment.event;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.pmease.gitplex.core.model.CommitComment;

public class CommitCommentRemoved extends CommitCommentEvent {

	public CommitCommentRemoved(AjaxRequestTarget target, CommitComment comment) {
		super(target, comment);
	}

}
