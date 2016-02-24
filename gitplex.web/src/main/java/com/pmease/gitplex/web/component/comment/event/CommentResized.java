package com.pmease.gitplex.web.component.comment.event;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.pmease.gitplex.core.entity.Comment;

public class CommentResized extends CommentEvent {

	public CommentResized(AjaxRequestTarget target, Comment comment) {
		super(target, comment);
	}

}
