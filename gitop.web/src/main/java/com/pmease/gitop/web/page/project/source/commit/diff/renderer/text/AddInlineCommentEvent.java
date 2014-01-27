package com.pmease.gitop.web.page.project.source.commit.diff.renderer.text;

import org.apache.wicket.ajax.AjaxRequestTarget;

public class AddInlineCommentEvent extends InlineCommentEvent {

	public AddInlineCommentEvent(AjaxRequestTarget target, int position) {
		super(target, position);
	}
}
