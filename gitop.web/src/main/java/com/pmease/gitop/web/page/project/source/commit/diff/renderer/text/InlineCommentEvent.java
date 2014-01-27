package com.pmease.gitop.web.page.project.source.commit.diff.renderer.text;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.pmease.gitop.web.common.wicket.event.AjaxEvent;

public abstract class InlineCommentEvent extends AjaxEvent {

	private final int position;
	private final String lineId;
	
	public InlineCommentEvent(AjaxRequestTarget target, int position, String lineId) {
		super(target);
		
		this.position = position;
		this.lineId = lineId;
	}

	public int getPosition() {
		return position;
	}

	public String getLineId() {
		return lineId;
	}
}
