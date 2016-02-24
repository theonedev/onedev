package com.pmease.gitplex.web.component.comment.event;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.pmease.commons.wicket.AjaxEvent;
import com.pmease.gitplex.core.entity.Comment;

public class CommentEvent extends AjaxEvent {

	private final Comment comment;
	
	public CommentEvent(AjaxRequestTarget target, Comment comment) {
		super(target);
		
		this.comment = comment;
	}

	public Comment getComment() {
		return comment;
	}

}
