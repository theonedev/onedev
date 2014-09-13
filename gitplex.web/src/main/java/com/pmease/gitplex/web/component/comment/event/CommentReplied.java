package com.pmease.gitplex.web.component.comment.event;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.pmease.gitplex.core.comment.CommentReply;
import com.pmease.gitplex.web.common.wicket.event.AjaxEvent;

public class CommentReplied extends AjaxEvent {

	private final CommentReply reply;
	
	public CommentReplied(AjaxRequestTarget target, CommentReply reply) {
		super(target);
		
		this.reply = reply;
	}

	public CommentReply getReply() {
		return reply;
	}

}
