package com.pmease.gitop.web.page.project.source.commit.diff.renderer.text;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.pmease.gitop.web.common.wicket.event.AjaxEvent;

public class ExpandContextEvent extends AjaxEvent {

	public static enum Direction {
		ABOVE, BELOW
	}
	
	private final Direction direction;
	
	public ExpandContextEvent(AjaxRequestTarget target, Direction direction) {
		super(target);
		this.direction = direction;
	}

	public Direction getDirection() {
		return direction;
	}

}
