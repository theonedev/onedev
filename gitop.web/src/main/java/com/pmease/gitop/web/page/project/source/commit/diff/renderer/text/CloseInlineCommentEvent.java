package com.pmease.gitop.web.page.project.source.commit.diff.renderer.text;

import org.apache.wicket.ajax.AjaxRequestTarget;

public class CloseInlineCommentEvent extends InlineCommentEvent {

	public CloseInlineCommentEvent(AjaxRequestTarget target, int position,
			String lineId) {
		super(target, position, lineId);
	}

	
}
