package com.pmease.gitop.web.component.comment;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.pmease.gitop.model.CommitComment;
import com.pmease.gitop.web.common.wicket.event.AjaxEvent;

public class CommitCommentRemoved extends AjaxEvent {

	private final CommitComment comment;
	
	public CommitCommentRemoved(AjaxRequestTarget target, CommitComment comment) {
		super(target);
		this.comment = comment;
	}

	public CommitComment getCommitComment() {
		return comment;
	}
}
