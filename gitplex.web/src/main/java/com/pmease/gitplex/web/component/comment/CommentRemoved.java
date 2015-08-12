package com.pmease.gitplex.web.component.comment;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.pmease.commons.wicket.AjaxEvent;
import com.pmease.gitplex.core.comment.Comment;

public class CommentRemoved extends AjaxEvent {

	private final Comment comment;
	
	public CommentRemoved(AjaxRequestTarget target, Comment comment) {
		super(target);
		
		this.comment = comment;
	}

	public Comment getComment() {
		return comment;
	}

}
