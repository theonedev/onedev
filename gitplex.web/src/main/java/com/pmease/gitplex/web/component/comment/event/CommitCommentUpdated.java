package com.pmease.gitplex.web.component.comment.event;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.pmease.gitplex.core.model.OldCommitComment;

public class CommitCommentUpdated extends CommitCommentEvent {

	public CommitCommentUpdated(AjaxRequestTarget target, OldCommitComment comment) {
		super(target, comment);
	}

}
