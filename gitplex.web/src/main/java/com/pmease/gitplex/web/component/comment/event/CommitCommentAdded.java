package com.pmease.gitplex.web.component.comment.event;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.pmease.gitplex.core.model.OldCommitComment;

public class CommitCommentAdded extends CommitCommentEvent {

	public CommitCommentAdded(AjaxRequestTarget target, OldCommitComment comment) {
		super(target, comment);
	}

}
