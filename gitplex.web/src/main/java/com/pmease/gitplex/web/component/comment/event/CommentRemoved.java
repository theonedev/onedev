package com.pmease.gitplex.web.component.comment.event;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.pmease.gitplex.core.entity.Comment;

public class CommentRemoved extends CommentEvent {

	public CommentRemoved(AjaxRequestTarget target, Comment comment) {
		super(target, comment);
	}

}
