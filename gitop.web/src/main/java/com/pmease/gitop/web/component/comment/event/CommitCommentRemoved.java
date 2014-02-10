package com.pmease.gitop.web.component.comment.event;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.pmease.gitop.model.CommitComment;

public class CommitCommentRemoved extends CommitCommentEvent {

	public CommitCommentRemoved(AjaxRequestTarget target, CommitComment comment) {
		super(target, comment);
	}

}
