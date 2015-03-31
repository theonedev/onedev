package com.pmease.gitplex.web.component.comment.event;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.pmease.commons.wicket.AjaxEvent;

public class AbstractLineCommentEvent extends AjaxEvent {

	final String lineId;
	final int position;
	
	public AbstractLineCommentEvent(AjaxRequestTarget target, int position, String lineId) {
		super(target);
		
		this.lineId = lineId;
		this.position = position;
	}

	public String getLineId() {
		return lineId;
	}

	public int getPosition() {
		return position;
	}
}
