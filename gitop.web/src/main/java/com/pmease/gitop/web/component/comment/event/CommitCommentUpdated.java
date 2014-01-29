package com.pmease.gitop.web.component.comment.event;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.pmease.gitop.model.CommitComment;

public class CommitCommentUpdated extends CommitCommentEvent {

	public CommitCommentUpdated(AjaxRequestTarget target, CommitComment comment) {
		super(target, comment);
	}

}
