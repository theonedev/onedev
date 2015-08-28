package com.pmease.gitplex.web.component.comment;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.pmease.commons.wicket.AjaxEvent;
import com.pmease.gitplex.core.model.PullRequestComment;

public class CommentRemoved extends AjaxEvent {

	private final PullRequestComment comment;
	
	public CommentRemoved(AjaxRequestTarget target, PullRequestComment comment) {
		super(target);
		
		this.comment = comment;
	}

	public PullRequestComment getComment() {
		return comment;
	}

}
