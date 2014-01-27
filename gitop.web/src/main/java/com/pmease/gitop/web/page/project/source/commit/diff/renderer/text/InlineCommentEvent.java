package com.pmease.gitop.web.page.project.source.commit.diff.renderer.text;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.pmease.gitop.web.common.wicket.event.AjaxEvent;

public class InlineCommentEvent extends AjaxEvent {

	private final int position;
	
	public InlineCommentEvent(AjaxRequestTarget target, int position) {
		super(target);
		
		this.position = position;
	}

	public int getPosition() {
		return position;
	}

}
