package com.pmease.gitplex.web.component.comment.event;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.pmease.gitplex.core.model.CommitComment;

public class CommitCommentAdded extends CommitCommentEvent {

	public CommitCommentAdded(AjaxRequestTarget target, CommitComment comment) {
		super(target, comment);
	}

}
