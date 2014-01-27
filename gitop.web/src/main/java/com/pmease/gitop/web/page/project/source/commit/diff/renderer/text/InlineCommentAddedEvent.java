package com.pmease.gitop.web.page.project.source.commit.diff.renderer.text;

import org.apache.wicket.ajax.AjaxRequestTarget;

public class InlineCommentAddedEvent extends InlineCommentEvent {

	private final Long commentId;
	
	public InlineCommentAddedEvent(AjaxRequestTarget target, int position, Long commentId) {
		super(target, position);
		
		this.commentId = commentId;
	}

	public Long getCommentId() {
		return commentId;
	}
}
